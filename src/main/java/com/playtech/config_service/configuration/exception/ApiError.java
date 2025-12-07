package com.playtech.config_service.configuration.exception;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public record ApiError(
        Instant timestamp,
        HttpStatus status,
        String message,
        String path
) {
}