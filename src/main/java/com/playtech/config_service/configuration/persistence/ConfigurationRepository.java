package com.playtech.config_service.configuration.persistence;

import com.playtech.config_service.configuration.model.Configuration;
import com.playtech.config_service.configuration.model.ConfigurationRequest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

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
}