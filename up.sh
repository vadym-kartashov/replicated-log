#!/bin/bash

set -e
mvn clean install -DskipTests=true
docker-compose down -v
# Deploy services using docker-compose
docker-compose up --build --remove-orphans
exit 0