package com.ying.learneyjourney.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;
@Data
public class AiGatewayException extends RuntimeException {
    private final HttpStatus status;

    public AiGatewayException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
