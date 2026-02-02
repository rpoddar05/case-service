# Local Infrastructure

This folder contains Docker Compose configuration required to run
case-service locally.
-This infrastructure is intended for local development only
-Application services connect to these containers via localhost ports 

## Services Included
- PostgreSQL (application database)
- Redpanda (Kafka-compatible broker)
- Redpanda Console (UI for Kafka topics and consumers)

## Prerequisites
- Docker
- Docker Compose

## Start local infrastructure
```bash
docker-compose up -d

