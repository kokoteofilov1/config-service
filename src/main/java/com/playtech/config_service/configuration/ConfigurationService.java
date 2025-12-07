package com.playtech.config_service.configuration;

import com.playtech.config_service.configuration.model.Configuration;
import com.playtech.config_service.configuration.model.ConfigurationRequest;
import com.playtech.config_service.configuration.persistence.ConfigurationRepository;
import com.playtech.config_service.configuration.util.ConfigurationValidator;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

    public List<Configuration> getConfigurations(String serviceName,
                                                 String environment,
                                                 String key) {
        final List<Configuration> configurations;

        if (environment == null && key == null) {
            configurations = configurationRepository.findByService(serviceName);
        } else if (key == null) {
            configurations = configurationRepository.findByServiceAndEnvironment(serviceName, environment);
        } else {
            configurations = configurationRepository.findByServiceAndEnvironmentAndKey(serviceName,
                                                                                       environment,
                                                                                       key);
        }

        LOGGER.info("Read {} configurations for service={}, environment={}, key={}",
                    configurations.size(),
                    serviceName,
                    environment,
                    key);

        return configurations;
    }
}
