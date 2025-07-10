# ---- Base image (default fallback) ----
ARG BASE_IMAGE
FROM ${BASE_IMAGE:-openjdk:21-jdk-slim} AS builder


# Set the working directory
WORKDIR /app

# Copy all files to the working directory
COPY . .


# Build the application using Gradle
RUN ./gradlew assemble -Dorg.gradle.daemon=false

# Use Eclipse Temurin JRE for running the application
FROM eclipse-temurin:21.0.7_6-jre-jammy

# Set the maintainer label
LABEL maintainer="@hmcts/cp-x-cjs-data-api"

# Update and upgrade the base image
RUN apt-get update && \
    apt-get -y upgrade && \
    rm -rf /var/lib/apt/lists/*

# Create a system user and group for running the application
RUN addgroup --gid 2000 --system appgroup && \
    adduser --uid 2000 --system appuser --gid 2000

# Set the working directory
WORKDIR /app

# Copy the built application JAR from the builder stage
COPY --from=builder --chown=appuser:appgroup /app/build/libs/service-cp-case-urn-mapper-*.jar /app/app.jar

# Copy the Java agent (if required)
#COPY --chown=appuser:appgroup agent.jar /app/agent.jar

# Set the user to the created system user
USER 2000

# Define the entry point for the container
ENTRYPOINT ["java", "-jar", "/app/app.jar"]


# Expose the application port
EXPOSE 4550
