package com.ying.learneyjourney.service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.ying.learneyjourney.config.StripeConfig;
import com.ying.learneyjourney.constaint.PlanType;
import com.ying.learneyjourney.constaint.TransactionStatus;
import com.ying.learneyjourney.constaint.TransactionType;
import com.ying.learneyjourney.dto.request.CreateCheckoutSessionRequest;
import com.ying.learneyjourney.dto.response.CheckoutSessionResponse;
import com.ying.learneyjourney.dto.response.WalletResponse;
import com.ying.learneyjourney.entity.BillingTransaction;
import com.ying.learneyjourney.entity.CreditWallet;
import com.ying.learneyjourney.entity.Subscription;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.exception.BadRequestException;
import com.ying.learneyjourney.exception.NotFoundException;
import com.ying.learneyjourney.repository.BillingTransactionRepository;
import com.ying.learneyjourney.repository.CreditWalletRepository;
import com.ying.learneyjourney.repository.SubscriptionRepository;
import com.ying.learneyjourney.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;

import static com.ying.learneyjourney.service.WorksheetGenerationServiceImpl.FREE_DAILY_GENERATION_LIMIT;

@Service
@RequiredArgsConstructor
@Transactional
public class BillingServiceImpl implements BillingService {

    private static final Map<String, Integer> CREDIT_PACKS = Map.of(
            "PACK_10", 10,
            "PACK_25", 25
    );

    private static final Map<String, Integer> GENERATION_CREDITS_PACKS = Map.of(
            "PACK_10", 100,
            "PACK_25", 500
    );

    // Stripe uses smallest currency unit
    // 99 THB = 9900 satang
    // 199 THB = 19900 satang
    private static final Map<String, Integer> CREDIT_PRICES = Map.of(
            "PACK_10", 9900,
            "PACK_25", 19900
    );

    private static final int FREE_DAILY_GENERATION_LIMIT = 5;
    private static final int FREE_EXPORT_LIMIT = 2;

    private static final String SUBSCRIPTION_CODE = "PRO_MONTHLY";
    private static final int PRO_MONTHLY_PRICE = 19900; // 199 THB

    private final UserRepository userRepository;
    private final CreditWalletRepository walletRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BillingTransactionRepository transactionRepository;
    private final StripeConfig stripeApiProperties;

    @Override
    public WalletResponse getWallet(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        CreditWallet wallet = getOrCreateWallet(user);
        boolean hasActiveSubscription = hasActiveSubscription(user);

        return new WalletResponse(
                user.getPlanType().name(),
                hasActiveSubscription,
                wallet.getCredits(),
                user.getFreeExportsUsed() == null ? 0 : user.getFreeExportsUsed(),
                user.getFreeExportsLimit() == null ? 2 : user.getFreeExportsLimit(),
                user.getDailyGenerationsUsed() == null ? 0 : user.getDailyGenerationsUsed(),
                FREE_DAILY_GENERATION_LIMIT,
                user.getGenerationCredits() == null ? 0 : user.getGenerationCredits()
        );
    }

    @Override
    public CheckoutSessionResponse createCheckoutSession(CreateCheckoutSessionRequest request, String userId) {
        validateStripeConfig();

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        getOrCreateWallet(user);

        try {
            if ("credits".equalsIgnoreCase(request.mode())) {
                return createCreditCheckoutSession(user, request.itemCode(), request.successUrl(), request.cancelUrl());
            }

            if ("subscription".equalsIgnoreCase(request.mode())) {
                return createSubscriptionCheckoutSession(user, request.itemCode(), request.successUrl(), request.cancelUrl());
            }

            throw new BadRequestException("Invalid checkout mode. Use 'credits' or 'subscription'.");

        } catch (StripeException e) {
            throw new BadRequestException("Unable to create Stripe checkout session: " + e.getMessage());
        }
    }

    @Override
    public void handleCheckoutCompleted(String sessionId) {
        BillingTransaction transaction = transactionRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        // idempotency: if webhook retries, don't apply twice
        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            return;
        }

        User user = transaction.getUser();

        if (transaction.getType() == TransactionType.CREDIT_PURCHASE) {
            // Apply Export Credits
            CreditWallet wallet = getOrCreateWallet(user);
            int creditsToAdd = transaction.getCreditsAdded() != null ? transaction.getCreditsAdded() : 0;
            wallet.setCredits(wallet.getCredits() + creditsToAdd);
            walletRepository.save(wallet);

            // Apply Generation Credits if it's a known pack
            String packageCode = transaction.getCourseId();
            Integer generationCredits = GENERATION_CREDITS_PACKS.get(packageCode);
            if (generationCredits != null) {
                user.setGenerationCredits((user.getGenerationCredits() == null ? 0 : user.getGenerationCredits()) + generationCredits);
                userRepository.save(user);
            }
        }

        if (transaction.getType() == TransactionType.SUBSCRIPTION) {
            deactivateCurrentSubscriptions(user);
            activateNewSubscription(user);
            user.setPlanType(PlanType.PRO);
            userRepository.save(user);
        }

        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);
    }

    @Override
    public boolean hasActiveSubscription(User user) {
        return subscriptionRepository.existsByUserIdAndActiveTrueAndEndDateAfter(
                user.getId(),
                OffsetDateTime.now()
        );
    }

    @Override
    public void consumeExport(User user) {

        if (hasActiveSubscription(user)) {
            return;
        }

        if (canUseFreeExport(user)) {
            int used = user.getFreeExportsUsed() == null ? 0 : user.getFreeExportsUsed();
            user.setFreeExportsUsed(used + 1);
            userRepository.save(user);
            return;
        }

        CreditWallet wallet = getOrCreateWallet(user);

        if (wallet.getCredits() == null || wallet.getCredits() <= 0) {
            throw new BadRequestException("No export credits available. You have used all free exports. Buy a package or subscribe.");
        }

        wallet.setCredits(wallet.getCredits() - 1);
        walletRepository.save(wallet);
    }

    private boolean canUseFreeExport(User user) {
        Integer used = user.getFreeExportsUsed() == null ? 0 : user.getFreeExportsUsed();
        Integer limit = user.getFreeExportsLimit() == null ? FREE_EXPORT_LIMIT : user.getFreeExportsLimit();
        return used < limit;
    }

    private CheckoutSessionResponse createCreditCheckoutSession(User user, String packageCode, String successUrl, String cancelUrl) throws StripeException {
        Integer credits = CREDIT_PACKS.get(packageCode);
        Integer unitAmount = CREDIT_PRICES.get(packageCode);

        if (credits == null || unitAmount == null) {
            throw new BadRequestException("Invalid credit package.");
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .putMetadata("type", "credits")
                .putMetadata("userEmail", user.getEmail())
                .putMetadata("packageCode", packageCode)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("thb")
                                                .setUnitAmount(unitAmount.longValue())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Worksheet Credits - " + packageCode)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);

        BillingTransaction transaction = new BillingTransaction();
        transaction.setUser(user);
        transaction.setType(TransactionType.CREDIT_PURCHASE);
        transaction.setAmount(unitAmount / 100);
        transaction.setCreditsAdded(credits);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setStripeSessionId(session.getId());
        transaction.setCourseId(packageCode); // Reusing courseId field to store packageCode

        transactionRepository.save(transaction);

        return new CheckoutSessionResponse(session.getId(), session.getUrl());
    }

    private CheckoutSessionResponse createSubscriptionCheckoutSession(User user, String itemCode, String successUrl, String cancelUrl) throws StripeException {
        if (!SUBSCRIPTION_CODE.equalsIgnoreCase(itemCode)) {
            throw new BadRequestException("Invalid subscription code.");
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .putMetadata("type", "subscription")
                .putMetadata("userEmail", user.getEmail())
                .putMetadata("planCode", itemCode)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("thb")
                                                .setUnitAmount((long) PRO_MONTHLY_PRICE)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Worksheet Pro Monthly")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);

        BillingTransaction transaction = new BillingTransaction();
        transaction.setUser(user);
        transaction.setType(TransactionType.SUBSCRIPTION);
        transaction.setAmount(PRO_MONTHLY_PRICE / 100);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setStripeSessionId(session.getId());

        transactionRepository.save(transaction);

        return new CheckoutSessionResponse(session.getId(), session.getUrl());
    }

    private void activateNewSubscription(User user) {
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlanName("PRO");
        subscription.setStartDate(OffsetDateTime.now());
        subscription.setEndDate(OffsetDateTime.now().plusMonths(1));
        subscription.setActive(true);

        subscriptionRepository.save(subscription);
    }

    private void deactivateCurrentSubscriptions(User user) {
        subscriptionRepository.findTopByUserIdAndActiveTrueOrderByEndDateDesc(user.getId())
                .ifPresent(subscription -> {
                    subscription.setActive(false);
                    subscriptionRepository.save(subscription);
                });
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private CreditWallet getOrCreateWallet(User user) {
        return walletRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                            CreditWallet wallet = new CreditWallet();
                            wallet.setUser(user);
                            wallet.setCredits(0);
                    walletRepository.save(wallet);
                            return wallet;
                        }
                );
    }

    private void validateStripeConfig() {
        if (isBlank(stripeApiProperties.getSecretKey())) {
            throw new BadRequestException("Stripe secret key is not configured.");
        }
        if (isBlank(stripeApiProperties.getSuccessUrl())) {
            throw new BadRequestException("Stripe success URL is not configured.");
        }
        if (isBlank(stripeApiProperties.getCancelUrl())) {
            throw new BadRequestException("Stripe cancel URL is not configured.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
