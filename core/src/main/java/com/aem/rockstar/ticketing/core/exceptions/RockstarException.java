package com.aem.rockstar.ticketing.core.exceptions;

public class RockstarException extends RuntimeException {
    public RockstarException(String userMessage, Throwable cause) {
        super(userMessage, cause);
    }
}
