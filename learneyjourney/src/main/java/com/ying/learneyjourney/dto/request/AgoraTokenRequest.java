package com.ying.learneyjourney.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AgoraTokenRequest {
    @NotBlank
    private String channel;

    @NotNull
    @Min(1)
    private Long uid;
}
