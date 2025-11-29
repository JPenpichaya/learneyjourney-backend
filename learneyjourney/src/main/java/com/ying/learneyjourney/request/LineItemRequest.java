package com.ying.learneyjourney.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class LineItemRequest {
    private String priceId;   // Preferred: pass Price ID directly
    private String productId; // Optional: if you prefer product → choose default price
    @Min(1)
    private Integer qty;
}
