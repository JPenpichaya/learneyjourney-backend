package com.ying.learneyjourney.service;

import com.stripe.model.checkout.Session;
import com.ying.learneyjourney.entity.Purchase;
import com.ying.learneyjourney.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class PaymentService {

    private final PurchaseRepository purchaseRepository;

    public PaymentService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    public void handleCheckoutSessionCompleted(Session session) {
        String sessionId = session.getId();

        // ✅ Idempotency: if we already processed this session, skip
        if (purchaseRepository.findByStripeSessionId(sessionId).isPresent()) {
            return;
        }

        String userId = session.getMetadata().get("userId");
        String courseId = session.getMetadata().get("courseId");

        Long amount = session.getAmountTotal();  // in smallest currency unit
        String currency = session.getCurrency();
        String paymentIntentId = session.getPaymentIntent();

        Purchase purchase = new Purchase();
        purchase.setUserId(userId);
        purchase.setCourseId(courseId);
        purchase.setAmount(amount);
        purchase.setCurrency(currency);
        purchase.setStripeSessionId(sessionId);
        purchase.setStripePaymentIntentId(paymentIntentId);
        purchase.setStatus("PAID");
        purchase.setPurchasedAt(OffsetDateTime.now());

        purchaseRepository.save(purchase);

        // 🤝 Here you can also:
        // - Mark course as unlocked for this user
        // - Send confirmation email
        // - Log analytics
    }
    public void saveCoursePurchaseFromWebhook(
            String stripeSessionId,
            String paymentIntentId,
            String userId,
            String courseId,
            Long amount,
            String currency
    ) {
        System.out.println("💾 Saving purchase for user " + userId + " course " + courseId);

        // Example idempotency: don't double-insert same session
        if (purchaseRepository.findByStripeSessionId(stripeSessionId).isPresent()) {
            System.out.println("⚠️ Purchase already exists for session " + stripeSessionId);
            return;
        }

        Purchase purchase = new Purchase();
        purchase.setStripeSessionId(stripeSessionId);
        purchase.setStripePaymentIntentId(paymentIntentId);
        purchase.setUserId(userId);
        purchase.setCourseId(courseId);
        purchase.setAmount(amount);
        purchase.setCurrency(currency);
        purchase.setStatus("PAID");
        purchase.setPurchasedAt(OffsetDateTime.now());

        purchaseRepository.save(purchase);

        System.out.println("✅ Purchase saved!");
    }
}
