package com.ying.learneyjourney.service;

import com.stripe.model.checkout.Session;
import com.ying.learneyjourney.constaint.EnumEnrollmentStatus;
import com.ying.learneyjourney.dto.EnrollmentDto;
import com.ying.learneyjourney.entity.Purchase;
import com.ying.learneyjourney.repository.PurchaseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PurchaseRepository purchaseRepository;
    private final EnrollmentService enrollmentService;

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
    @Transactional
    public void saveCoursePurchaseFromWebhook(
            String stripeSessionId,
            String paymentIntentId,
            String userId,
            String courseId,
            Long amount,
            String currency,
            String eventId
    ) {
        System.out.println("💾 Saving purchase for user " + userId + " course " + courseId);

        if(purchaseRepository.exist_StripEvenId(eventId)){
            System.out.println("⚠️ Purchase already exists for event " + eventId);
            return;
        }

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
        purchase.setStripeEventId(eventId);

        purchaseRepository.save(purchase);

        EnrollmentDto enrollmentDto = new EnrollmentDto();
        enrollmentDto.setUserId(userId);
        enrollmentDto.setCourseId(UUID.fromString(courseId));
        enrollmentDto.setProgress(0);
        enrollmentDto.setStatus(EnumEnrollmentStatus.NOT_START);

        enrollmentService.create(enrollmentDto);

        System.out.println("✅ Purchase saved!");
    }
}
