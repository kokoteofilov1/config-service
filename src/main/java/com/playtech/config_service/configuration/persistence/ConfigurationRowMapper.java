package com.playtech.config_service.configuration.persistence;

import com.playtech.config_service.configuration.model.enums.ConfigStatus;
import com.playtech.config_service.configuration.model.enums.ConfigValueType;
import com.playtech.config_service.configuration.model.Configuration;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class ConfigurationRowMapper implements RowMapper<Configuration> {
    @Override
    public Configuration mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Configuration(rs.getLong("id"),
                                 rs.getString("service_name"),
                                 rs.getString("environment"),
                                 rs.getString("config_key"),
                                 rs.getString("config_value"),
                                 ConfigValueType.valueOf(rs.getString("type")),
                                 rs.getString("description"),
                                 rs.getInt("version"),
                                 ConfigStatus.valueOf(rs.getString("status")),
                                 rs.getObject("created_at", OffsetDateTime.class),
                                 rs.getString("created_by"),
                                 rs.getObject("updated_at", OffsetDateTime.class),
                                 rs.getString("updated_by"));
    }
}