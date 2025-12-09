To start the application using the **dev** profile (which also starts the Docker Compose services such as Postgres), run the following command from the project root:

`./gradlew bootRun --args='--spring.profiles.active=dev'`

# Design

`config-service` acts as a central place for managing configurations for other microservices.

When a new configuration is added, services will receive a Kafka event containing all of the relevant information. 

Additionally, services can always fetch the latest configuration via a REST endpoint. There is also a more generic endpoint that can be used for reading multiple configurations, which might be useful for creating dashboards or analyzing the available configurations.

## Data Model

For this service to be useful we need to know the following about each configuration.

- Which **service** it’s targeting.
- Which **environment** it’s targeting.
- The **key** identifying this configuration.
- The **value.**
- **Type** - `STRING` , `INT` , `BOOLEAN` , etc.
- Human-readable **description**.
- **Version**.
- **Status** - `ACTIVE`, `DISABLED`, `DEPRECATED`, etc.
- **When** it was **created.**
- **Who created** the configuration.
- **When** it was **last updated.**
- **Who** **updated** the configuration.

## Versioning of managed configurations

Configurations are versioned.

A configuration is **uniquely identified by the combination of service name, environment, configuration key and version**.

When a configuration is created, if there is already an existing configuration with the same unique identifier, the old one will be marked as deprecated and a new one will be created with the next version number.

The history is preserved and configuration changes can be audited.

# API

### POST /configurations

**Example request body:**

```java
{
  "serviceName": "string",
  "environment": "string",
  "key": "string",
  "value": "string",
  "type": "STRING",
  "description": "string",
  "status": "ACTIVE",
  "user": "string"
}
```

### GET /configurations

**Query parameters:**

- serviceName (required)
- environment (optional)
- key (optional)

**Example response body**:

```json
[
  {
    "id": 0,
    "serviceName": "string",
    "environment": "string",
    "key": "string",
    "value": "string",
    "type": "STRING",
    "description": "string",
    "version": 0,
    "status": "ACTIVE",
    "createdAt": "2025-12-09T09:29:07.749Z",
    "createdBy": "string",
    "updatedAt": "2025-12-09T09:29:07.749Z",
    "updatedBy": "string"
  }
]
```

### GET /configurations/latest

**Query parameters:**

- serviceName (required)
- environment (required)
- key (required)

**Example response body:**

```json
{
  "id": 0,
  "serviceName": "string",
  "environment": "string",
  "key": "string",
  "value": "string",
  "type": "STRING",
  "description": "string",
  "version": 0,
  "status": "ACTIVE",
  "createdAt": "2025-12-09T09:32:36.687Z",
  "createdBy": "string",
  "updatedAt": "2025-12-09T09:32:36.687Z",
  "updatedBy": "string"
}
```

# Technical Considerations

### Caching

Both endpoints will utilize **caching**. Whenever a new configuration is created, it will automatically be populated (write-through) into the latest configurations cache.

This will also result in the invalidation of the cache for the generic lookup endpoint for all cache entries that hold configurations for the same service.

The generic lookups will be cached using the cache-aside strategy - only when the data is requested.

### Notifications

Services will receive notifications via Kafka events when new configurations are created. Initially the plan is to use a single topic, that all services will read from, but this can later be extended by having dedicated topic per environment and routing configuration events dynamically.