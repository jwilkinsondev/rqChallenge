package com.reliaquest.api.exceptions;

public class ExternalApiRateLimitException extends RuntimeException {
    public ExternalApiRateLimitException(String message) {
        super(message);
    }
}
