package com.playtech.config_service.configuration.persistence;

import com.playtech.config_service.configuration.model.Configuration;
import com.playtech.config_service.configuration.model.ConfigurationRequest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ConfigurationRepository {

    private final static String INSERT_CONFIGURATION_SQL = """
            INSERT INTO configurations (
                    service_name,
                    environment,
                    config_key,
                    config_value,
                    type,
                    description,
                    status,
                    created_by
            ) VALUES (
                    :service_name,
                    :environment,
                    :config_key,
                    :config_value,
                    :type,
                    :description,
                    CAST(:status AS config_status),
                    :created_by
            );
            """;

    private final static String FIND_CONFIGURATION_BY_SERVICE = """
            SELECT *
            FROM configurations
            WHERE service_name = :service_name
            """;

    private final static String FIND_CONFIGURATION_BY_SERVICE_AND_ENVIRONMENT = """
            SELECT *
            FROM configurations
            WHERE service_name = :service_name
            AND environment = :environment
            """;

    private static final String FIND_CONFIGURATION_BY_SERVICE_AND_ENVIRONMENT_AND_KEY = """
            SELECT *
            FROM configurations
            WHERE service_name = :service_name
            AND environment = :environment
            AND config_key = :config_key
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Configuration> rowMapper = new ConfigurationRowMapper();

    public ConfigurationRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long insert(ConfigurationRequest configuration) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("service_name", configuration.serviceName());
        params.addValue("environment", configuration.environment());
        params.addValue("config_key", configuration.key());
        params.addValue("config_value", configuration.value());
        params.addValue("type", configuration.type().name());
        params.addValue("description", configuration.description());
        params.addValue("status", configuration.status().name());
        params.addValue("created_by", configuration.user());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(INSERT_CONFIGURATION_SQL,
                            params,
                            keyHolder,
                            new String[]{"id"});

        return keyHolder.getKeyAs(Long.class);
    }

    public List<Configuration> findConfigurationsByService(String serviceName) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("service_name", serviceName);

        return jdbcTemplate.query(FIND_CONFIGURATION_BY_SERVICE,
                                  params,
                                  rowMapper);
    }

    public List<Configuration> findConfigurationsByServiceAndEnvironment(String serviceName,
                                                                         String environment) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("service_name", serviceName);
        params.addValue("environment", environment);

        return jdbcTemplate.query(FIND_CONFIGURATION_BY_SERVICE_AND_ENVIRONMENT,
                                  params,
                                  rowMapper);
    }

    public List<Configuration> findConfigurationByServiceAndEnvironmentAndKey(String serviceName,
                                                                              String environment,
                                                                              String key) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("service_name", serviceName);
        params.addValue("environment", environment);
        params.addValue("config_key", key);

        return jdbcTemplate.query(FIND_CONFIGURATION_BY_SERVICE_AND_ENVIRONMENT_AND_KEY,
                                  params,
                                  rowMapper);
    }
}