package com.playtech.config_service.configuration.util;

import com.playtech.config_service.configuration.model.ConfigurationRequest;

public class ConfigurationValidator {

    public static void validateConfigurationRequest(ConfigurationRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("ConfigurationRequest cannot be null");
        }

        requireNonEmpty(req.serviceName(), "serviceName");
        requireNonEmpty(req.environment(), "environment");
        requireNonEmpty(req.key(), "key");
        requireNonEmpty(req.value(), "value");
        requireNonNull(req.type(), "type");
        requireNonEmpty(req.description(), "description");
        requireNonNull(req.status(), "status");
        requireNonEmpty(req.user(), "user");
    }

    private static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + fieldName + "' must not be null or empty");
        }
    }

    private static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException("Field '" + fieldName + "' must not be null");
        }
    }
}