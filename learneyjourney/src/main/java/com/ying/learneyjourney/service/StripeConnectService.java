package com.ying.learneyjourney.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.ying.learneyjourney.config.StripeConfig;
import com.ying.learneyjourney.dto.response.StripeConnectOnboardingResponse;
import com.ying.learneyjourney.entity.TutorProfile;
import com.ying.learneyjourney.repository.TutorProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StripeConnectService {

    private final TutorProfileService tutorApplicationService;
    private final TutorProfileRepository repository;
    private final StripeConfig stripeProperties;

    @Transactional
    public StripeConnectOnboardingResponse createOrResumeOnboarding(UUID applicationId) throws StripeException, StripeException {
        TutorProfile app = tutorApplicationService.get(applicationId);

        if (app.getStripeConnectAccountId() == null) {
            AccountCreateParams accountParams = AccountCreateParams.builder()
                    .setType(AccountCreateParams.Type.STANDARD)
                    .setCountry(app.getCountry() != null ? app.getCountry() : "TH")
                    .setEmail(app.getEmail())
                    .setBusinessType(AccountCreateParams.BusinessType.INDIVIDUAL)
                    .build();

            Account account = Account.create(accountParams);
            app.setStripeConnectAccountId(account.getId());
            repository.save(app);
        }

        AccountLinkCreateParams linkParams = AccountLinkCreateParams.builder()
                .setAccount(app.getStripeConnectAccountId())
                .setRefreshUrl(stripeProperties.getConnect().getRefreshUrl() + "?applicationId=" + app.getId())
                .setReturnUrl(stripeProperties.getConnect().getReturnUrl() + "?applicationId=" + app.getId())
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();

        AccountLink accountLink = AccountLink.create(linkParams);

        return new StripeConnectOnboardingResponse(
                app.getStripeConnectAccountId(),
                accountLink.getUrl()
        );
    }

    @Transactional
    public void refreshAccountState(String accountId) throws StripeException {
        TutorProfile app = repository.findByStripeConnectAccountId(accountId).orElseThrow(() -> new IllegalArgumentException("Tutor application not found for Stripe account"));

        Account account = Account.retrieve(accountId);
        app.setChargesEnabled(account.getChargesEnabled());
        app.setPayoutsEnabled(account.getPayoutsEnabled());
        app.setConnectOnboardingComplete(Boolean.TRUE.equals(account.getDetailsSubmitted()));
        repository.save(app);
    }


    @Transactional
    public void syncAccountState(UUID applicationId) throws StripeException {
        TutorProfile app = repository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Tutor application not found"));

        if (app.getStripeConnectAccountId() == null || app.getStripeConnectAccountId().isBlank()) {
            throw new IllegalStateException("Stripe connected account has not been created yet");
        }

        Account account = Account.retrieve(app.getStripeConnectAccountId());
        app.setConnectOnboardingComplete(Boolean.TRUE.equals(account.getDetailsSubmitted()));
        app.setChargesEnabled(Boolean.TRUE.equals(account.getChargesEnabled()));
        app.setPayoutsEnabled(Boolean.TRUE.equals(account.getPayoutsEnabled()));
        repository.save(app);
    }

}