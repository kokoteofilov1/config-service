package com.playtech.config_service.configuration;

import com.playtech.config_service.configuration.model.Configuration;
import com.playtech.config_service.configuration.model.ConfigurationRequest;
import com.playtech.config_service.configuration.persistence.ConfigurationRepository;
import com.playtech.config_service.configuration.util.ConfigurationValidator;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class ConfigurationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationService.class);

    private final ConfigurationRepository configurationRepository;

    public ConfigurationService(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    public Long createConfiguration(ConfigurationRequest configurationRequest) {
        ConfigurationValidator.validateConfigurationRequest(configurationRequest);

        final Long insertedId;

        final Optional<Configuration> latest =
                configurationRepository.findLatestByServiceAndEnvironmentAndKey(configurationRequest.serviceName(),
                                                                                configurationRequest.environment(),
                                                                                configurationRequest.key());

        if (latest.isEmpty()) {
            insertedId = configurationRepository.insert(configurationRequest, 1);

            LOGGER.info("Created configuration: service={}, env={}, key={}, createdBy={}",
                        configurationRequest.serviceName(),
                        configurationRequest.environment(),
                        configurationRequest.key(),
                        configurationRequest.user());

            return insertedId;
        }

        final Integer latestVersion = latest.get().version();

        deprecateExistingConfigurations(configurationRequest.serviceName(),
                                        configurationRequest.environment(),
                                        configurationRequest.key());

        insertedId = configurationRepository.insert(configurationRequest, latestVersion + 1);

        LOGGER.info("Updated configuration to version={}: service={}, env={}, key={}, createdBy={}",
                    latestVersion,
                    configurationRequest.serviceName(),
                    configurationRequest.environment(),
                    configurationRequest.key(),
                    configurationRequest.user());

        return insertedId;
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

    public Optional<Configuration> getLatestConfiguration(String serviceName,
                                                          String environment,
                                                          String key) {
        final Optional<Configuration> latest = configurationRepository.findLatestByServiceAndEnvironmentAndKey(serviceName,
                                                                                                               environment,
                                                                                                               key);

        LOGGER.info("Read latest configuration for service={}, environment={}, key={}",
                    serviceName,
                    environment,
                    key);

        return latest;
    }

    private void deprecateExistingConfigurations(String serviceName,
                                                 String environment,
                                                 String key) {
        configurationRepository.deprecateExisting(serviceName, environment, key);

        LOGGER.info("Deprecated existing configurations for service={}, environment={}, key={}",
                    serviceName,
                    environment,
                    key);
    }
}
