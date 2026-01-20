package com.ying.learneyjourney.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PurchaseDto {
    private UUID id;
    private String userId;
    private Long amount;
    private String currency;
    private String status;
    private String stripeSessionId;
    private String stripePaymentIntentId;
    private UUID courseId;
    private String stripeEventId;

    public static PurchaseDto fromEntity(com.ying.learneyjourney.entity.Purchase purchase) {
        PurchaseDto dto = new PurchaseDto();
        dto.setId(purchase.getId());
        dto.setUserId(purchase.getUserId());
        dto.setAmount(purchase.getAmount());
        dto.setCurrency(purchase.getCurrency());
        dto.setStatus(purchase.getStatus());
        dto.setStripeSessionId(purchase.getStripeSessionId());
        dto.setStripePaymentIntentId(purchase.getStripePaymentIntentId());
        dto.setCourseId(purchase.getCourseId() != null ? UUID.fromString(purchase.getCourseId()) : null);
        dto.setStripeEventId(purchase.getStripeEventId());
        return dto;
    }

    public static com.ying.learneyjourney.entity.Purchase toEntity(PurchaseDto dto) {
        com.ying.learneyjourney.entity.Purchase purchase = new com.ying.learneyjourney.entity.Purchase();
        purchase.setId(dto.getId());
        purchase.setUserId(dto.getUserId());
        purchase.setAmount(dto.getAmount());
        purchase.setCurrency(dto.getCurrency());
        purchase.setStatus(dto.getStatus());
        purchase.setStripeSessionId(dto.getStripeSessionId());
        purchase.setStripePaymentIntentId(dto.getStripePaymentIntentId());
        purchase.setCourseId(dto.getCourseId() != null ? dto.getCourseId().toString() : null);
        purchase.setStripeEventId(dto.getStripeEventId());
        return purchase;
    }
}
