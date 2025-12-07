package com.playtech.config_service.configuration.persistence;

import com.playtech.config_service.configuration.model.Configuration;
import com.playtech.config_service.configuration.model.ConfigurationRequest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
                    version,
                    status,
                    created_by
            ) VALUES (
                    :service_name,
                    :environment,
                    :config_key,
                    :config_value,
                    :type,
                    :description,
                    :version,
                    CAST(:status AS config_status),
                    :created_by
            )
            RETURNING *;
            """;

    private final static String UPDATE_CONFIGURATION_STATUS_TO_DEPRECATED_SQL = """
            UPDATE configurations
            SET status = 'DEPRECATED'
            WHERE service_name = :service_name
            AND environment = :environment
            AND config_key = :config_key
            AND status <> 'DEPRECATED'
            """;

    private final static String FIND_CONFIGURATION_BY_SERVICE_SQL = """
            SELECT *
            FROM configurations
            WHERE service_name = :service_name
            """;

    private final static String FIND_CONFIGURATION_BY_SERVICE_AND_ENVIRONMENT_SQL = """
            SELECT *
            FROM configurations
            WHERE service_name = :service_name
            AND environment = :environment
            """;

    private static final String FIND_CONFIGURATION_BY_SERVICE_AND_ENVIRONMENT_AND_KEY_SQL = """
            SELECT *
            FROM configurations
            WHERE service_name = :service_name
            AND environment = :environment
            AND config_key = :config_key
            """;

    private static final String FIND_LATEST_CONFIGURATION_BY_SERVICE_AND_ENVIRONMENT_AND_KEY_SQL = """
            SELECT *
            FROM configurations
            WHERE service_name = :service_name
              AND environment = :environment
              AND config_key = :config_key
            ORDER BY version DESC
            LIMIT 1;
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Configuration> rowMapper = new ConfigurationRowMapper();

    public ConfigurationRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Configuration insert(ConfigurationRequest configuration, int version) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("service_name", configuration.serviceName());
        params.addValue("environment", configuration.environment());
        params.addValue("config_key", configuration.key());
        params.addValue("config_value", configuration.value());
        params.addValue("type", configuration.type().name());
        params.addValue("description", configuration.description());
        params.addValue("version", version);
        params.addValue("status", configuration.status().name());
        params.addValue("created_by", configuration.user());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        return jdbcTemplate.queryForObject(INSERT_CONFIGURATION_SQL,
                                           params,
                                           rowMapper);
    }

    public List<Configuration> findByService(String serviceName) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("service_name", serviceName);

        return jdbcTemplate.query(FIND_CONFIGURATION_BY_SERVICE_SQL,
                                  params,
                                  rowMapper);
    }

    public List<Configuration> findByServiceAndEnvironment(String serviceName,
                                                           String environment) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("service_name", serviceName);
        params.addValue("environment", environment);

        return jdbcTemplate.query(FIND_CONFIGURATION_BY_SERVICE_AND_ENVIRONMENT_SQL,
                                  params,
                                  rowMapper);
    }

    public List<Configuration> findByServiceAndEnvironmentAndKey(String serviceName,
                                                                 String environment,
                                                                 String key) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("service_name", serviceName);
        params.addValue("environment", environment);
        params.addValue("config_key", key);

        return jdbcTemplate.query(FIND_CONFIGURATION_BY_SERVICE_AND_ENVIRONMENT_AND_KEY_SQL,
                                  params,
                                  rowMapper);
    }

    public Optional<Configuration> findLatestByServiceAndEnvironmentAndKey(String serviceName,
                                                                           String environment,
                                                                           String key) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("service_name", serviceName);
        params.addValue("environment", environment);
        params.addValue("config_key", key);

        return jdbcTemplate.query(FIND_LATEST_CONFIGURATION_BY_SERVICE_AND_ENVIRONMENT_AND_KEY_SQL,
                                  params,
                                  rowMapper)
                           .stream()
                           .findFirst();
    }

    public void deprecateExisting(String serviceName,
                                  String environment,
                                  String key) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("service_name", serviceName);
        params.addValue("environment", environment);
        params.addValue("config_key", key);

        jdbcTemplate.update(UPDATE_CONFIGURATION_STATUS_TO_DEPRECATED_SQL, params);
    }
}