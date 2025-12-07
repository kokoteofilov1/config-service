package com.playtech.config_service.configuration;

import com.playtech.config_service.configuration.model.Configuration;
import com.playtech.config_service.configuration.model.ConfigurationCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class ConfigurationUpdatePublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationUpdatePublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String configurationUpdateTopicName;

    public ConfigurationUpdatePublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                        @Value("${config.kafka.topic}") String configurationUpdateTopicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.configurationUpdateTopicName = configurationUpdateTopicName;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishConfigurationUpdate(ConfigurationCreatedEvent configurationCreatedEvent) {
        final Configuration configuration = configurationCreatedEvent.configuration();

        final String messageKey = new StringBuilder().append(configuration.serviceName())
                                                     .append("-")
                                                     .append(configuration.environment())
                                                     .append("-").append(configuration.key())
                                                     .toString();

        kafkaTemplate.send(configurationUpdateTopicName, messageKey, configuration);

        LOGGER.info("Published configuration update to topic={}",
                    configurationUpdateTopicName);
    }
}
