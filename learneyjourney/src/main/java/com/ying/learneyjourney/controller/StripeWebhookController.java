package com.ying.learneyjourney.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.Transfer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.TransferCreateParams;
import com.ying.learneyjourney.dto.PurchaseDto;
import com.ying.learneyjourney.entity.Orders;
import com.ying.learneyjourney.entity.Purchase;
import com.ying.learneyjourney.entity.StripeTransfer;
import com.ying.learneyjourney.entity.TutorProfile;
import com.ying.learneyjourney.master.BusinessException;
import com.ying.learneyjourney.repository.OrdersRepository;
import com.ying.learneyjourney.repository.PurchaseRepository;
import com.ying.learneyjourney.repository.StripeTransferRepository;
import com.ying.learneyjourney.repository.TutorProfileRepository;
import com.ying.learneyjourney.service.PaymentService;
import com.ying.learneyjourney.service.StripeTransferService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stripe")
@Slf4j
@Profile("!test")
@RequiredArgsConstructor
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PurchaseRepository purchaseRepository;
    private final TutorProfileRepository tutorProfileRepository;
    private final StripeTransferService stripeTransferService;

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(HttpServletRequest request) {

        try {
            // 1️⃣ Read RAW payload (CRITICAL)
            String payload = request.getReader()
                    .lines()
                    .collect(Collectors.joining("\n"));

            // 2️⃣ Read Stripe signature header
            String sigHeader = request.getHeader("Stripe-Signature");
            if (sigHeader == null) {
                log.error("❌ Missing Stripe-Signature header");
                return ResponseEntity.badRequest().body("Missing Stripe-Signature");
            }

            // 3️⃣ Verify + construct event
            Event event = Webhook.constructEvent(
                    payload,
                    sigHeader,
                    webhookSecret
            );

            log.info("✅ Stripe webhook received: id={}, type={}", event.getId(), event.getType());

            // 4️⃣ Handle ONLY what you support
            if ("checkout.session.completed".equals(event.getType())) {

                Optional<StripeObject> objectOpt =
                        event.getDataObjectDeserializer().getObject();

                if (objectOpt.isPresent()) {
                    // ✅ Normal path (older API versions)
                    Session session = (Session) objectOpt.get();
                    handleSession(session, event.getId());

                } else {
                    // ⚠️ Fallback path (new API versions)
                    log.warn("⚠️ Stripe SDK could not deserialize event object. Using fallback. eventId={}", event.getId());

                    JsonNode root = objectMapper.readTree(payload);
                    JsonNode sessionNode = root.path("data").path("object");

                    handleSessionFallback(sessionNode, event.getId());
                }
            }

            // 5️⃣ ALWAYS return 200 for handled events
            return ResponseEntity.ok("ok");

        } catch (SignatureVerificationException e) {
            log.error("❌ Invalid Stripe signature", e);
            return ResponseEntity.status(400).body("Invalid signature");

        } catch (Exception e) {
            // IMPORTANT: still return 200 to avoid infinite retries
            log.error("❌ Stripe webhook error", e);
            return ResponseEntity.ok("error handled");
        }
    }

    @PostMapping("/webhook/courses")
    public ResponseEntity<String> webhookCourses(HttpServletRequest request) {

        try {
            // 1️⃣ Read RAW payload (CRITICAL)
            String payload = request.getReader()
                    .lines()
                    .collect(Collectors.joining("\n"));

            // 2️⃣ Read Stripe signature header
            String sigHeader = request.getHeader("Stripe-Signature");
            if (sigHeader == null) {
                log.error("❌ Missing Stripe-Signature header");
                return ResponseEntity.badRequest().body("Missing Stripe-Signature");
            }

            // 3️⃣ Verify + construct event
            Event event = Webhook.constructEvent(
                    payload,
                    sigHeader,
                    webhookSecret
            );

            log.info("✅ Stripe webhook received: id={}, type={}", event.getId(), event.getType());

            // 4️⃣ Handle ONLY what you support
            if ("checkout.session.completed".equals(event.getType())) {

                Optional<StripeObject> objectOpt =
                        event.getDataObjectDeserializer().getObject();

                if (objectOpt.isPresent()) {
                    // ✅ Normal path (older API versions)
                    Session session = (Session) objectOpt.get();
                    handleSession(session, event.getId());

                } else {
                    // ⚠️ Fallback path (new API versions)
                    log.warn("⚠️ Stripe SDK could not deserialize event object. Using fallback. eventId={}", event.getId());

                    JsonNode root = objectMapper.readTree(payload);
                    JsonNode sessionNode = root.path("data").path("object");
                    String orderId = sessionNode.path("metadata").path("orderId").asText(null);

                    handleSessionFallback(sessionNode, event.getId());

                    List<Purchase> purchaseList = purchaseRepository.findby_orderId(orderId);

                    if(purchaseList.isEmpty()){
                        throw new BusinessException("No purchases found for order " + orderId, "PURCHASES_NOT_FOUND");
                    }

                    for (Purchase p : purchaseList) {
                        // Optional: add your own idempotency key per transfer if you store purchase IDs
                        com.ying.learneyjourney.entity.TutorProfile tutorProfile = tutorProfileRepository.findTutorProfileByPurchaseId(p.getId()).orElseThrow(() -> new BusinessException("Tutor Stripe account not found for purchase " + p.getId(), "TUTOR_STRIPE_ACCOUNT_NOT_FOUND"));

                        Transfer t = Transfer.create(
                                TransferCreateParams.builder()
                                        .setAmount(p.getAmount())
                                        .setCurrency("usd")
                                        .setDestination(tutorProfile.getStripConnect())
                                        .setTransferGroup(orderId)
                                        .build()
                        );
                        // You may want to store t.getId() per purchase in DB
                        stripeTransferService.update(p, t, "COMPLETED");
                    }

                }
            }

            // 5️⃣ ALWAYS return 200 for handled events
            return ResponseEntity.ok("ok");

        } catch (SignatureVerificationException e) {
            log.error("❌ Invalid Stripe signature", e);
            return ResponseEntity.status(400).body("Invalid signature");

        } catch (Exception e) {
            // IMPORTANT: still return 200 to avoid infinite retries
            log.error("❌ Stripe webhook error", e);
            return ResponseEntity.ok("error handled");
        }
    }

    // ===== Handlers =====

    private void handleSession(Session session, String eventId) {

        paymentService.saveCoursePurchaseFromWebhook(
                session.getId(),
                session.getPaymentIntent(),
                session.getMetadata().get("userId"),
                session.getMetadata().get("courseId"),
                session.getAmountTotal(),
                session.getCurrency(),
                eventId,
                "PAID",
                session.getMetadata().get("orderId"));
    }

    private void handleSessionFallback(JsonNode s, String eventId) {

        paymentService.saveCoursePurchaseFromWebhook(
                s.path("id").asText(),
                s.path("payment_intent").asText(null),
                s.path("metadata").path("userId").asText(null),
                s.path("metadata").path("courseId").asText(null),
                s.path("amount_total").asLong(),
                s.path("currency").asText(),
                eventId,
                "FAILED",
                s.path("metadata").path("orderId").asText(null)
        );
    }
}
