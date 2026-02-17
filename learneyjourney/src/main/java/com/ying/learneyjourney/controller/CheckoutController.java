package com.ying.learneyjourney.controller;
import com.google.firebase.auth.FirebaseToken;
import com.stripe.model.Price;
import com.stripe.model.PriceCollection;
import com.stripe.model.Product;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.ying.learneyjourney.Util.FirebaseAuthUtil;
import com.ying.learneyjourney.entity.Purchase;
import com.ying.learneyjourney.entity.TutorProfile;
import com.ying.learneyjourney.master.BusinessException;
import com.ying.learneyjourney.repository.TutorProfileRepository;
import com.ying.learneyjourney.request.CreateSessionRequest;
import com.ying.learneyjourney.request.LineItemRequest;
import com.ying.learneyjourney.service.OrdersService;
import com.ying.learneyjourney.service.PaymentService;
import com.ying.learneyjourney.service.StripeTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/checkout")
@Profile("!test")
@RequiredArgsConstructor
public class CheckoutController {

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    private final PaymentService paymentService;
    private final OrdersService ordersService;
    private final StripeTransferService stripeTransferService;
    private final TutorProfileRepository tutorProfileRepository;

    @Value("${app.require-basic-auth:true}")
    private boolean requireBasicAuth;

    @PostMapping("/create-session")
    public ResponseEntity<Map<String, Object>> createCheckoutSession(
            @Valid @RequestBody CreateSessionRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        String userId;
        String email;

        if (requireBasicAuth) {
            if (authHeader == null || authHeader.isBlank()) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "error", "UNAUTHORIZED",
                        "message", "Missing Authorization header"
                ));
            }
            FirebaseToken decoded = FirebaseAuthUtil.verify(authHeader);

            userId = decoded.getUid(); // ✅ THIS IS YOUR USER ID
            email = decoded.getEmail();
        }else{
            userId = "V3Mai7cJY8Xt5F0KVhusMQ7kycs2";
            email = "codebytechademics@gmail.com";

        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one item is required");
        }

        try {
            List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

            for (LineItemRequest item : request.getItems()) {
                String priceId = item.getPriceId();

                // Optional: allow using productId instead of priceId
                if ((priceId == null || priceId.isBlank()) && item.getProductId() != null) {
                    // Prefer the product's default price; otherwise pick an active one-time price
                    Product product = Product.retrieve(item.getProductId());
                    Object defaultPriceObj = product.getDefaultPrice();
                    if (defaultPriceObj instanceof String s && !s.isBlank()) {
                        priceId = s;
                    } else {
                        Map<String, Object> params = new HashMap<>();
                        params.put("product", item.getProductId());
                        params.put("active", true);
                        params.put("limit", 10);
                        PriceCollection pc = Price.list(params);
                        Price chosen = pc.getData().stream()
                                .filter(p -> "one_time".equals(p.getType()))
                                .findFirst()
                                .orElse(pc.getData().isEmpty() ? null : pc.getData().get(0));
                        if (chosen == null) {
                            throw new IllegalArgumentException("No active prices for product " + item.getProductId());
                        }
                        priceId = chosen.getId();
                    }
                }

                if (priceId == null || priceId.isBlank()) {
                    throw new IllegalArgumentException("Each item must have either priceId or productId.");
                }

                if (item.getQty() <= 0) {
                    throw new IllegalArgumentException("Quantity must be > 0");
                }

                lineItems.add(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(priceId)
                                .setQuantity(Long.valueOf(item.getQty()))
                                .build()
                );
            }

            SessionCreateParams.Builder builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(request.getSuccessUrl())   // use server-side config
                    .setCancelUrl(request.getCancelUrl())     // use server-side config
                    .addAllLineItem(lineItems)
                    .putMetadata("userId", userId)        // you'd add this field to your request DTO
                    .putMetadata("courseId", request.getCourseId().toString());

//            if(lineItems.stream().anyMatch(li -> li.getPrice().equals("price_1SHfYuP3M4EMXc3By7FzJBg6"))){
//                builder.setShippingAddressCollection(
//                        SessionCreateParams.ShippingAddressCollection.builder()
//                                .addAllowedCountry(SessionCreateParams
//                                        .ShippingAddressCollection.AllowedCountry.TH) // Thailand
//                                // .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.US) // add more if needed
//                                .build()
//                );
//            }

            if (request.getCustomerEmail() != null && !request.getCustomerEmail().isBlank()) {
                builder.setCustomerEmail(request.getCustomerEmail());
            }

            // (Optional) also collect phone
            builder.setPhoneNumberCollection(
                    SessionCreateParams.PhoneNumberCollection.builder().setEnabled(true).build()
            );
            SessionCreateParams params = builder.build();

            Session session = Session.create(params);

            Map<String, Object> resp = new HashMap<>();
            resp.put("id", session.getId());
            resp.put("url", session.getUrl());
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
    }


    @PostMapping("/create-session/courses")
    public ResponseEntity<Map<String, Object>> createCheckoutSessionCourse(
            @Valid @RequestBody CreateSessionRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one item is required");
        }

        FirebaseToken decoded = FirebaseAuthUtil.verify(authHeader);

        String userId = decoded.getUid(); // ✅ THIS IS YOUR USER ID
        String email = decoded.getEmail(); // optional
        String orderId = ordersService.create(request, "PENDING");
        try {
            List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

            for (LineItemRequest item : request.getItems()) {
                String priceId = item.getPriceId();

                // Optional: allow using productId instead of priceId
                if ((priceId == null || priceId.isBlank()) && item.getProductId() != null) {
                    // Prefer the product's default price; otherwise pick an active one-time price
                    Product product = Product.retrieve(item.getProductId());
                    Object defaultPriceObj = product.getDefaultPrice();
                    if (defaultPriceObj instanceof String s && !s.isBlank()) {
                        priceId = s;
                    } else {
                        Map<String, Object> params = new HashMap<>();
                        params.put("product", item.getProductId());
                        params.put("active", true);
                        params.put("limit", 10);
                        PriceCollection pc = Price.list(params);
                        Price chosen = pc.getData().stream()
                                .filter(p -> "one_time".equals(p.getType()))
                                .findFirst()
                                .orElse(pc.getData().isEmpty() ? null : pc.getData().get(0));
                        if (chosen == null) {
                            throw new IllegalArgumentException("No active prices for product " + item.getProductId());
                        }
                        priceId = chosen.getId();
                    }
                }

                if (priceId == null || priceId.isBlank()) {
                    throw new IllegalArgumentException("Each item must have either priceId or productId.");
                }

                if (item.getQty() <= 0) {
                    throw new IllegalArgumentException("Quantity must be > 0");
                }

                lineItems.add(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(priceId)
                                .setQuantity(Long.valueOf(item.getQty()))
                                .build()
                );

                Purchase purchase = paymentService.savePurchase(
                        null, null, userId, item.getCourseId().toString(), item.getAmount(),
                        item.getCurrency(), null, "PENDING", orderId
                );
                TutorProfile tutorProfile = tutorProfileRepository.findTutorProfileByPurchaseId(purchase.getId()).orElseThrow(() -> new BusinessException("Tutor Stripe account not found for purchase " + purchase.getId(), "TUTOR_STRIPE_ACCOUNT_NOT_FOUND"));
                stripeTransferService.create(null, purchase, tutorProfile, null, "PENDING");
            }

            SessionCreateParams.Builder builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)   // use server-side config
                    .setCancelUrl(cancelUrl)     // use server-side config
                    .addAllLineItem(lineItems)
                    .putMetadata("orderId", orderId);

//            if(lineItems.stream().anyMatch(li -> li.getPrice().equals("price_1SHfYuP3M4EMXc3By7FzJBg6"))){
//                builder.setShippingAddressCollection(
//                        SessionCreateParams.ShippingAddressCollection.builder()
//                                .addAllowedCountry(SessionCreateParams
//                                        .ShippingAddressCollection.AllowedCountry.TH) // Thailand
//                                // .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.US) // add more if needed
//                                .build()
//                );
//            }

            if (request.getCustomerEmail() != null && !request.getCustomerEmail().isBlank()) {
                builder.setCustomerEmail(request.getCustomerEmail());
            }

            // (Optional) also collect phone
            builder.setPhoneNumberCollection(
                    SessionCreateParams.PhoneNumberCollection.builder().setEnabled(true).build()
            );
            SessionCreateParams params = builder.build();

            Session session = Session.create(params);

            Map<String, Object> resp = new HashMap<>();
            resp.put("id", session.getId());
            resp.put("url", session.getUrl());
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
    }
}

