# Docker Compose Setup

This project is composed of several independent Docker services such as PostgreSQL, Redis, Kafka, Prometheus, and
Grafana.
To make it easier to manage and run all containers at once, we use a script-based approach with multiple Compose files.

## Structure

```
docker/
├── base/
│ ├── docker-compose.postgres.yml
│ ├── docker-compose.redis.yml
│ ├── docker-compose.pgadmin.yml
│ ├── docker-compose.mailhog.yml
│ └── docker-compose.zookeeper-kafka.yml
├── monitoring/
│ ├── docker-compose.prometheus.yml
│ └── docker-compose.grafana.yml
setup.sh

```

## How to Start All Services

Simply run the following script to start all services in the background:

```bash
  ./setup.sh
```
