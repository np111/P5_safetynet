# Deployment

## Compilation

Compile the server with maven:
```
mvn package
```

Then deploy the server jar (`server/target/safetynet-alerts-server.jar`) on a
Java 8+ execution environment.

## Configuration

The only supported and tested SQL database is MySQL 5.7!

Configure the server with environment variables:
- `SERVER_PORT`: The http listening port (default: 8081)
- `MYSQL_HOST`: The SQL database host (default: localhost)
- `MYSQL_PORT`: The SQL database port (default: 3306)
- `MYSQL_DATABASE`: The SQL database name (default: safetynet)
- `MYSQL_USERNAME`: The SQL database username
- `MYSQL_PASSWORD`: The SQL database password
- `HTTP_LOGGING_ENABLED`: Whether or not http logging is enabled (default: false).
  Prefer a reverse proxy for logging.

## Execution

Run the server jar:
```
java -jar <path/to/safetynet-alerts-server.jar>
```

Important: It is recommended to use a reverse proxy or API gateway to publicly
serve the HTTP endpoints (to setup TLS, authorization, logging, ...).
