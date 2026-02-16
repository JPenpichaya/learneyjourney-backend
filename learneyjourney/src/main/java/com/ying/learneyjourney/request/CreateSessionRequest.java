package com.ying.learneyjourney.request;

import com.ying.learneyjourney.request.LineItemRequest;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateSessionRequest {
    @NotEmpty
    private List<LineItemRequest> items;
    private String customerEmail;
    private String userId;
    private UUID courseId;
    private Long amount;
    private String currency;
    private String successUrl;
    private String cancelUrl;
}
