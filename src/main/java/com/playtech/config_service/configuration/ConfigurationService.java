package com.playtech.config_service.configuration;

import com.playtech.config_service.configuration.model.Configuration;
import com.playtech.config_service.configuration.model.ConfigurationCreatedEvent;
import com.playtech.config_service.configuration.model.ConfigurationRequest;
import com.playtech.config_service.configuration.persistence.ConfigurationRepository;
import com.playtech.config_service.configuration.util.ConfigurationValidator;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ConfigurationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationService.class);

    private final ConfigurationRepository configurationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ConfigurationService(ConfigurationRepository configurationRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.configurationRepository = configurationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long createConfiguration(ConfigurationRequest configurationRequest) {
        ConfigurationValidator.validateConfigurationRequest(configurationRequest);

        final Optional<Configuration> latest =
                configurationRepository.findLatestByServiceAndEnvironmentAndKey(configurationRequest.serviceName(),
                                                                                configurationRequest.environment(),
                                                                                configurationRequest.key());

        final boolean isCreate = latest.isEmpty();
        final int nextVersion;

        if (isCreate) {
            nextVersion = 1;
        } else {
            nextVersion = latest.get().version() + 1;
            deprecateExistingConfigurations(configurationRequest.serviceName(),
                                            configurationRequest.environment(),
                                            configurationRequest.key());
        }

        final Configuration inserted = configurationRepository.insert(configurationRequest, nextVersion);

        LOGGER.info("{} configuration version={}: service={}, env={}, key={}, createdBy={}",
                    isCreate ? "Created" : "Updated",
                    nextVersion,
                    configurationRequest.serviceName(),
                    configurationRequest.environment(),
                    configurationRequest.key(),
                    configurationRequest.user());

        eventPublisher.publishEvent(new ConfigurationCreatedEvent(inserted));

        return inserted.id();
    }

    @Cacheable(
            cacheNames = "configurationsList",
            key = "T(String).format('%s-%s-%s', #serviceName, #environment == null ? '' : #environment, #key == null ? '' : #key)"
    )
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

    @Cacheable(
            cacheNames = "latestConfiguration",
            key = "T(String).format('%s-%s-%s', #serviceName, #environment, #key)"
    )
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
