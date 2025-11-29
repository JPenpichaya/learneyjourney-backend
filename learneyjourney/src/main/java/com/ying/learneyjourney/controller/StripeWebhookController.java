package com.ying.learneyjourney.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.ying.learneyjourney.service.PaymentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
@Slf4j
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;


    public StripeWebhookController(PaymentService paymentService,
                                   ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        log.info("🔔 Received REAL webhook");

        Event event;
        try {
            // ✅ 1) Verify the signature
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("❌ Invalid Stripe signature", e);
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        log.info("✅ Signature OK. Event type = {}", event.getType());

        // ✅ 2) Convert event.getData() back to Map (same as your /webhook-test)
        if ("checkout.session.completed".equals(event.getType())) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> payloadMap =
                        objectMapper.readValue(payload, Map.class);

                Map<String, Object> data = (Map<String, Object>) payloadMap.get("data");
                Map<String, Object> session = (Map<String, Object>) data.get("object");

                String sessionId = (String) session.get("id");
                String paymentIntentId = (String) session.get("payment_intent");
                Long amountTotal = ((Number) session.get("amount_total")).longValue();
                String currency = (String) session.get("currency");

                Map<String, Object> metadata = (Map<String, Object>) session.get("metadata");
                String userId = metadata != null ? (String) metadata.get("userId") : null;
                String courseId = metadata != null ? (String) metadata.get("courseId") : null;

                log.info("Parsed from REAL webhook: sessionId={}, userId={}, courseId={}, amount={}, currency={}",
                        sessionId, userId, courseId, amountTotal, currency);

                // ✅ 3) Reuse the SAME service method that works in /webhook-test
                paymentService.saveCoursePurchaseFromWebhook(
                        sessionId,
                        paymentIntentId,
                        userId,
                        courseId,
                        amountTotal,
                        currency
                );

            } catch (JsonProcessingException e) {
                log.error("❌ Failed to parse event JSON", e);
                // but still return 200 so Stripe won't spam retries
            }
        } else {
            log.info("ℹ️ Ignoring event type {}", event.getType());
        }

        return ResponseEntity.ok("success");
    }
    @PostMapping("/webhook-test")
    public ResponseEntity<String> handleTestWebhook(@RequestBody Map<String, Object> payload) {
        System.out.println("🔔 Received webhook-test payload");

        String type = (String) payload.get("type");
        System.out.println("Event type = " + type);

        if (!"checkout.session.completed".equals(type)) {
            return ResponseEntity.ok("ignored other event");
        }

        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        Map<String, Object> session = (Map<String, Object>) data.get("object");

        // Extract fields from the JSON you gave me
        String sessionId = (String) session.get("id");
        String paymentIntentId = (String) session.get("payment_intent");
        Long amountTotal = ((Number) session.get("amount_total")).longValue();
        String currency = (String) session.get("currency");

        Map<String, Object> metadata = (Map<String, Object>) session.get("metadata");
        String userId = metadata != null ? (String) metadata.get("userId") : null;
        String courseId = metadata != null ? (String) metadata.get("courseId") : null;

        System.out.printf("✅ Parsed sessionId=%s, userId=%s, courseId=%s, amount=%d %s%n",
                sessionId, userId, courseId, amountTotal, currency);

        // Call your service to save to DB
        paymentService.saveCoursePurchaseFromWebhook(
                sessionId,
                paymentIntentId,
                userId,
                courseId,
                amountTotal,
                currency
        );

        return ResponseEntity.ok("success");
    }
}