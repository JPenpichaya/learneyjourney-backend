package com.ying.learneyjourney.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BuyCreditsRequest(
        @NotBlank String userEmail,
        @NotBlank String packageType
) {
}