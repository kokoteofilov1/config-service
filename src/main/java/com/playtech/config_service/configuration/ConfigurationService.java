package com.playtech.config_service.configuration;

import com.playtech.config_service.configuration.model.ConfigurationRequest;
import com.playtech.config_service.configuration.persistence.ConfigurationRepository;
import com.playtech.config_service.configuration.util.ConfigurationValidator;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ConfigurationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationService.class);

    private final ConfigurationRepository configurationRepository;

    public ConfigurationService(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    public Long createConfiguration(ConfigurationRequest configurationRequest) {
        ConfigurationValidator.validateConfigurationRequest(configurationRequest);

        final Long id = configurationRepository.insert(configurationRequest);

        LOGGER.info("Created configuration: service={}, env={}, key={}, createdBy={}",
                    configurationRequest.serviceName(),
                    configurationRequest.environment(),
                    configurationRequest.key(),
                    configurationRequest.user());

        return id;
    }
}
