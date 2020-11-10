# [![SafetyNet Alerts](.readme/logo.png?raw=true)](https://github.com/np111/P5_safetynet)
[![Build Status](https://travis-ci.com/np111/P5_safetynet.svg?branch=master)](https://travis-ci.com/np111/P5_safetynet) [![codecov.io](https://codecov.io/github/np111/P5_safetynet/coverage.svg?branch=master)](https://codecov.io/github/np111/P5_safetynet?branch=master)

SafetyNet Alerts is a REST API that sends information to emergency services!

[![SafetyNet Alerts](.readme/console.png?raw=true)](https://github.com/np111/P5_safetynet)

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

See [DEPLOYMENT.md](./DEPLOYMENT.md).

## Notes

This is a school project (for OpenClassrooms).

The goal is to create a java http REST API.
