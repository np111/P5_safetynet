# SafetyNet Alerts &middot; [![Build Status](https://travis-ci.com/np111/P5_safetynet.svg?branch=master)](https://travis-ci.com/np111/P5_safetynet) [![codecov.io](https://codecov.io/github/np111/P5_safetynet/coverage.svg?branch=master)](https://codecov.io/github/np111/P5_safetynet?branch=master)

SafetyNet Alerts is a REST API that sends information to emergency services!

## Documentation

- [REST API](https://np111.github.io/P5_safetynet/httpapi/)
- Javadoc:
  - [safetynet-alerts-api](https://np111.github.io/P5_safetynet/javadoc/api/)
  - [safetynet-alerts-server](https://np111.github.io/P5_safetynet/javadoc/server/)

## Getting started

These instructions will get you a copy of the project up and running on your
local machine for development.

### Prerequisites

- Install [Maven 3.6+](https://maven.apache.org/download.cgi)
- Install [Java 8+](https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot)
- Install [Docker](https://docs.docker.com/get-docker/) and [Docker Compose](https://docs.docker.com/compose/install/)

### Running App

Start the docker development environment:
```
./dev.sh docker up -d
```

Compile the server with maven:
```
mvn package
```

And run it:
```
cd server/target/
java -jar safetynet-alerts-server.jar
```

### Testing

To run the tests, type:
```
mvn verify
```

## Deployment

### Compilation

Compile the server with maven:
```
mvn package
```

Then deploy the server jar (`server/target/safetynet-alerts-server.jar`) on a
Java 8+ execution environment.

### Configuration

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

### Execution

Run the server jar:
```
java -jar <path/to/safetynet-alerts-server.jar>
```

Important: It is recommended to use a reverse proxy or API gateway to publicly
serve the HTTP endpoints (to setup TLS, authorization, logging, ...).

## Notes

This is a school project (for OpenClassrooms).

The goal is to create a java http REST API.
