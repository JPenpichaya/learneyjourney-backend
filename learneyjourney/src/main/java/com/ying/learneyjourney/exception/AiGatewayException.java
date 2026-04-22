package com.ying.learneyjourney.exception;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
@Getter
@Setter
public class AiGatewayException extends RuntimeException {
    private final HttpStatus status;

    public AiGatewayException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
