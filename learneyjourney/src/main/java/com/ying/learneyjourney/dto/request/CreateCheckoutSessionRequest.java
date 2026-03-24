package com.ying.learneyjourney.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCheckoutSessionRequest(
        @NotBlank String userEmail,
        @NotBlank String mode,
        @NotBlank String itemCode
) {
}
