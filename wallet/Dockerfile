# syntax=docker/dockerfile:1

FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /build

RUN apk add --no-cache bash

COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x ./gradlew
COPY build.gradle ./

COPY src ./src

RUN ./gradlew war --no-daemon

FROM ghcr.io/platform-sean/launcher:latest

COPY --from=build /build/build/libs/wallet.war /app/

