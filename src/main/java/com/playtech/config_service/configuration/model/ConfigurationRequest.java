package com.playtech.config_service.configuration.model;

import com.playtech.config_service.configuration.model.enums.ConfigStatus;
import com.playtech.config_service.configuration.model.enums.ConfigValueType;

public record ConfigurationRequest(
        String serviceName,
        String environment,
        String key,
        String value,
        ConfigValueType type,
        String description,
        ConfigStatus status,
        String user
) {
}