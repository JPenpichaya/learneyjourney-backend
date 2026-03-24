package com.ying.learneyjourney.dto.request;

import java.util.List;

public record OpenAiChatRequest(
        String model,
        List<Message> messages,
        Double temperature
) {
    public record Message(String role, String content) {}
}
