package com.ying.learneyjourney.controller;
import com.stripe.model.Price;
import com.stripe.model.PriceCollection;
import com.stripe.model.Product;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.ying.learneyjourney.request.CreateSessionRequest;
import com.ying.learneyjourney.request.LineItemRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @PostMapping("/create-session")
    public ResponseEntity<Map<String, Object>> createCheckoutSession(
            @Valid @RequestBody CreateSessionRequest request) {

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
                    .setSuccessUrl(successUrl)   // use server-side config
                    .setCancelUrl(cancelUrl)     // use server-side config
                    .addAllLineItem(lineItems)
                    .putMetadata("userId", request.getUserId())        // you'd add this field to your request DTO
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
}

