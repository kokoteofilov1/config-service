package com.playtech.config_service.configuration.model;

import com.playtech.config_service.configuration.model.enums.ConfigStatus;
import com.playtech.config_service.configuration.model.enums.ConfigValueType;

import java.time.OffsetDateTime;

public record Configuration(
        Long id,
        String serviceName,
        String environment,
        String key,
        String value,
        ConfigValueType type,
        String description,
        Integer version,
        ConfigStatus status,
        OffsetDateTime createdAt,
        String createdBy,
        OffsetDateTime updatedAt,
        String updatedBy
) {
}