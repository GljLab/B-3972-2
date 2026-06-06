#!/bin/bash
mkdir -p backend/src/main/java/com/example/inventory/controller
mkdir -p backend/src/main/java/com/example/inventory/service
mkdir -p backend/src/main/java/com/example/inventory/mapper
mkdir -p backend/src/main/java/com/example/inventory/entity
mkdir -p backend/src/main/java/com/example/inventory/dto
mkdir -p backend/src/main/resources/mapper

mkdir -p frontend/src/views
mkdir -p frontend/src/components
mkdir -p frontend/src/store
mkdir -p frontend/src/api
mkdir -p frontend/src/router
mkdir -p frontend/src/assets

# Create config files
touch docker-compose.yml
touch init.sql
touch README.md

echo "Project structure created successfully."
