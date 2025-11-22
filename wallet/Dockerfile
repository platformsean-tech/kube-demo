# syntax=docker/dockerfile:1

# Build stage
FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /build

# Install bash for gradlew script
RUN apk add --no-cache bash

# Copy Gradle files
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x ./gradlew
COPY build.gradle ./

# Copy source
COPY src ./src

# Build WAR
RUN ./gradlew war --no-daemon

# Runtime stage - use launcher as base
FROM platformsean/launcher:latest

# Copy the built WAR file
COPY --from=build /build/build/libs/wallet.war /app/

