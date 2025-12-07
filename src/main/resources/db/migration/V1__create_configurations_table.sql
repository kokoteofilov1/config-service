CREATE TYPE config_status AS ENUM (
    'ACTIVE',
    'DEPRECATED',
    'DISABLED'
);

CREATE TABLE configurations (
    id           BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    environment  VARCHAR(50)  NOT NULL,
    config_key   VARCHAR(200) NOT NULL,
    config_value TEXT NOT NULL,
    type         VARCHAR(20)  NOT NULL,
    description  TEXT,
    version      INT NOT NULL DEFAULT 1,
    status       config_status NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by   VARCHAR(100),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by   VARCHAR(100)
);

CREATE UNIQUE INDEX uq_configurations_natural_key
  ON configurations(service_name, environment, config_key, version);