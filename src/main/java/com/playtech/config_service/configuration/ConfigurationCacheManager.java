package com.playtech.config_service.configuration;

import com.playtech.config_service.configuration.model.Configuration;
import com.playtech.config_service.configuration.model.ConfigurationCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class ConfigurationCacheManager {
    private final CacheManager cacheManager;
    private final String latestConfigurationCacheName;
    private final String configurationsListCacheName;

    public ConfigurationCacheManager(CacheManager cacheManager,
                                     @Value("${redis.configuration-write-through-cache}") String latestConfigurationCacheName,
                                     @Value("${redis.configuration-cache-aside-cache}") String configurationsListCacheName) {
        this.cacheManager = cacheManager;
        this.latestConfigurationCacheName = latestConfigurationCacheName;
        this.configurationsListCacheName = configurationsListCacheName;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void cacheLatestConfiguration(ConfigurationCreatedEvent configurationCreatedEvent) {
        final Configuration configuration = configurationCreatedEvent.configuration();

        final String cacheKey = new StringBuilder().append(configuration.serviceName())
                                                   .append("-")
                                                   .append(configuration.environment())
                                                   .append("-")
                                                   .append(configuration.key())
                                                   .toString();

        final Cache latestConfigurationsCache = cacheManager.getCache(latestConfigurationCacheName);

        if (latestConfigurationsCache != null) {
            latestConfigurationsCache.put(cacheKey, configuration);
        }

        //Invalidate all keys for this service in configuration list cache whenever a new configuration is inserted
        invalidateConfigurationsListCache(configuration);
    }

    private void invalidateConfigurationsListCache(Configuration configuration) {
        final String serviceOnlyKey = String.format("%s--", configuration.serviceName());

        final String serviceAndEnvKey = String.format("%s-%s-",
                                                      configuration.serviceName(),
                                                      configuration.environment());

        final String serviceEnvAndKey = String.format("%s-%s-%s",
                                                      configuration.serviceName(),
                                                      configuration.environment(),
                                                      configuration.key());


        final Cache configurationsListCache = cacheManager.getCache(configurationsListCacheName);

        if (configurationsListCache != null) {
            configurationsListCache.evict(serviceOnlyKey);
            configurationsListCache.evict(serviceAndEnvKey);
            configurationsListCache.evict(serviceEnvAndKey);
        }
    }
}
