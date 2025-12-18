package com.ying.learneyjourney.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.ying.learneyjourney.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/stripe")
@Slf4j
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final PaymentService paymentService;

    public StripeWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        final Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        final String eventId = event.getId();
        final String eventType = event.getType();
        log.info("Stripe webhook received: id={}, type={}", eventId, eventType);

        // ✅ Only handle the events you support
        if (!"checkout.session.completed".equals(eventType)) {
            return ResponseEntity.ok("ignored");
        }

        try {
            // ✅ Prefer Stripe deserializer (no need to re-parse JSON into Map)
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            Optional<StripeObject> stripeObjectOpt = deserializer.getObject();

            if (stripeObjectOpt.isEmpty()) {
                // Happens when API version mismatch or invalid payload shape
                log.error("Stripe webhook: unable to deserialize object. eventId={}", eventId);
                // Let Stripe retry (could be transient / API version issues)
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("deserialization_failed");
            }

            Session session = (Session) stripeObjectOpt.get();

            String sessionId = session.getId();
            String paymentIntentId = session.getPaymentIntent();
            Long amountTotal = session.getAmountTotal();
            String currency = session.getCurrency();

            Map<String, String> metadata = session.getMetadata();
            String userId = metadata != null ? metadata.get("userId") : null;
            String courseId = metadata != null ? metadata.get("courseId") : null;

            log.info("checkout.session.completed: eventId={}, sessionId={}, paymentIntentId={}, userId={}, courseId={}, amount={}, currency={}",
                    eventId, sessionId, paymentIntentId, userId, courseId, amountTotal, currency);

            // ✅ Call service (you will add idempotency inside service or before calling it)
            paymentService.saveCoursePurchaseFromWebhook(
                    sessionId,
                    paymentIntentId,
                    userId,
                    courseId,
                    amountTotal,
                    currency,
                    eventId // <-- add this param to support idempotency
            );

            return ResponseEntity.ok("success");

        } catch (Exception e) {
            // If DB is down or transaction fails, return 500 so Stripe retries later
            log.error("Stripe webhook processing failed: eventId={}", eventId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("processing_failed");
        }
    }
}
