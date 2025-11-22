# syntax=docker/dockerfile:1

FROM eclipse-temurin:25-jre-alpine

LABEL maintainer="Anonymous Software Solutions"

ENV PORT=8080
ENV HOST=0.0.0.0
ENV LOCATION=webapps
ENV ENTITY_SIZE=4096000
ENV CORS_HEADERS=""

RUN mkdir -p /logs && \
    addgroup -S launcher && \
    adduser -S launcher -G launcher

COPY ./build/libs/launcher.jar /launcher.jar

USER launcher

WORKDIR /app

CMD java -jar /launcher.jar -port ${PORT} -host ${HOST} -entitysize ${ENTITY_SIZE} -corsheaders "${CORS_HEADERS}" -location ${LOCATION} -war *.war
