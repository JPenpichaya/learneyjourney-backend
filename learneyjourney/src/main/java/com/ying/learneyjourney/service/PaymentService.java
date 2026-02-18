package com.ying.learneyjourney.service;

import com.stripe.model.checkout.Session;
import com.ying.learneyjourney.constaint.EnumEnrollmentStatus;
import com.ying.learneyjourney.dto.EnrollmentDto;
import com.ying.learneyjourney.dto.PurchaseDto;
import com.ying.learneyjourney.entity.Orders;
import com.ying.learneyjourney.entity.Purchase;
import com.ying.learneyjourney.master.BusinessException;
import com.ying.learneyjourney.repository.LessonProgressRepository;
import com.ying.learneyjourney.repository.OrdersRepository;
import com.ying.learneyjourney.repository.PurchaseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PurchaseRepository purchaseRepository;
    private final EnrollmentService enrollmentService;
    private final PostEnrollmentAsyncService postEnrollmentAsyncService;
    private final OrdersRepository ordersRepository;
    private final LessonProgressService lessonProgressService;
    private final VideoProgressService videoProgressService;

    public Purchase savePurchase(String sessionId,
                                    String paymentIntentId,
                                    String userId,
                                    UUID courseId,
                                    Long amount,
                                    String currency,
                                    String eventId,
                                    String status,
                                    String orderId) {


        Purchase purchase = new Purchase();
        purchase.setUserId(userId);
        purchase.setCourseId(courseId == null ? null : courseId.toString());
        purchase.setAmount(amount);
        purchase.setCurrency(currency);
        purchase.setStripeSessionId(sessionId);
        purchase.setStripePaymentIntentId(paymentIntentId);
        purchase.setStatus(status);
        purchase.setPurchasedAt(OffsetDateTime.now());
        purchase.setOrderId(orderId);

        purchaseRepository.save(purchase);
        return purchase;
    }
    @Transactional
    public void saveCoursePurchaseFromWebhook(
            String sessionId,
            String paymentIntentId,
            String userId,
            String courseId,
            Long amount,
            String currency,
            String eventId,
            String status,
            String orderId
    ) {
        System.out.println("💾 Saving purchase for user " + userId + " course " + courseId);

        log.info("""
                💰 Payment completed
                sessionId={}
                paymentIntentId={}
                userId={}
                courseId={}
                amount={}
                currency={}
                """,
                sessionId, paymentIntentId, userId, courseId, amount, currency
        );


        if(purchaseRepository.exist_StripEvenId(eventId)){
            System.out.println("⚠️ Purchase already exists for event " + eventId);
            return;
        }

        // Example idempotency: don't double-insert same session
        Optional<Purchase> existingPurchase = purchaseRepository.findByStripeSessionId(sessionId);
        if (existingPurchase.isPresent()) {
            Purchase p = existingPurchase.get();
            if ("PAID".equalsIgnoreCase(p.getStatus()) || "COMPLETED".equalsIgnoreCase(p.getStatus())) {
                System.out.println("⚠️ Purchase already processed for session " + sessionId);
                return;
            }
            // If it exists but is not PAID (e.g. PENDING), we continue to update it.
        }

        if(orderId != null){
            Orders orders = ordersRepository.findby_id(UUID.fromString(orderId)).orElseThrow(()-> new BusinessException("Order not found: " + orderId, "ORDER_NOT_FOUND"));
            orders.setStatus(status);
            ordersRepository.save(orders);
        }

        List<Purchase> purchases = purchaseRepository.findby_orderId(orderId);
        log.info("orderId={} purchasesFound={}", orderId, purchases.size());
        for(Purchase purchase : purchases){
            purchase.setStripeSessionId(sessionId);
            purchase.setStripePaymentIntentId(paymentIntentId);
            purchase.setStatus(status);
            purchase.setPurchasedAt(OffsetDateTime.now());
            purchase.setStripeEventId(eventId);
            purchaseRepository.save(purchase);

            if("FAILED".equals(status)){
                System.out.println("FAILED HERE");
                continue;
            }
            EnrollmentDto enrollmentDto = new EnrollmentDto();
            // Use the userId from the purchase record if available, otherwise fallback to metadata
            String targetUserId = purchase.getUserId() != null ? purchase.getUserId() : userId;
            
            // Use courseId from purchase record if available, otherwise fallback to metadata
            String targetCourseId = purchase.getCourseId() != null ? purchase.getCourseId() : courseId;

            if (targetCourseId == null) {
                log.error("❌ Course ID is null for purchase {}", purchase.getId());
                continue; // Skip this purchase if courseId is missing
            }

            enrollmentDto.setUserId(targetUserId);
            enrollmentDto.setCourseId(UUID.fromString(targetCourseId));
            enrollmentDto.setProgress(0);
            enrollmentDto.setStatus(EnumEnrollmentStatus.NOT_START);

            enrollmentService.create(enrollmentDto);
            lessonProgressService.setLessonProgressAllNotStart(targetUserId, UUID.fromString(targetCourseId));
            videoProgressService.setVideoProgressAllNotStart(targetUserId, UUID.fromString(targetCourseId));
        }

        System.out.println("✅ Purchase saved!");

        // Use the courseId from metadata for the email if available, otherwise try to find one from the purchases
        UUID emailCourseId = null;
        if (courseId != null) {
            emailCourseId = UUID.fromString(courseId);
        } else if (!purchases.isEmpty() && purchases.get(0).getCourseId() != null) {
            emailCourseId = UUID.fromString(purchases.get(0).getCourseId());
        }

        if (emailCourseId != null) {
            postEnrollmentAsyncService.sendingEmailAfterEnrolled(userId, emailCourseId, sessionId);
        } else {
            log.warn("⚠️ Could not determine courseId for email notification");
        }
    }

}
