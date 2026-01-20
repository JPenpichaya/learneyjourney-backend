package com.ying.learneyjourney.service;

import com.stripe.model.checkout.Session;
import com.ying.learneyjourney.constaint.EnumEnrollmentStatus;
import com.ying.learneyjourney.dto.EnrollmentDto;
import com.ying.learneyjourney.dto.PurchaseDto;
import com.ying.learneyjourney.entity.Orders;
import com.ying.learneyjourney.entity.Purchase;
import com.ying.learneyjourney.master.BusinessException;
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

    public Purchase savePurchase(String sessionId,
                                    String paymentIntentId,
                                    String userId,
                                    String courseId,
                                    Long amount,
                                    String currency,
                                    String eventId,
                                    String status,
                                    String orderId) {


        Purchase purchase = new Purchase();
        purchase.setUserId(userId);
        purchase.setCourseId(courseId);
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
        if (purchaseRepository.findByStripeSessionId(sessionId).isPresent()) {
            System.out.println("⚠️ Purchase already exists for session " + sessionId);
            return;
        }

        if(orderId != null){
            Orders orders = ordersRepository.findby_id(UUID.fromString(orderId)).orElseThrow(()-> new BusinessException("Order not found: " + orderId, "ORDER_NOT_FOUND"));
            orders.setStatus(status);
            ordersRepository.save(orders);
        }

        List<Purchase> purchases = purchaseRepository.findby_orderId(orderId);
        for(Purchase purchase : purchases){
            purchase.setStripeSessionId(sessionId);
            purchase.setStripePaymentIntentId(paymentIntentId);
            purchase.setStatus(status);
            purchase.setPurchasedAt(OffsetDateTime.now());
            purchase.setStripeEventId(eventId);
            purchaseRepository.save(purchase);

            if("FAILED".equals(status)){
                continue;
            }
            EnrollmentDto enrollmentDto = new EnrollmentDto();
            enrollmentDto.setUserId(userId);
            enrollmentDto.setCourseId(UUID.fromString(purchase.getCourseId()));
            enrollmentDto.setProgress(0);
            enrollmentDto.setStatus(EnumEnrollmentStatus.NOT_START);

            enrollmentService.create(enrollmentDto);
        }

        System.out.println("✅ Purchase saved!");

        postEnrollmentAsyncService.sendingEmailAfterEnrolled(userId, UUID.fromString(courseId), sessionId);
    }

}
