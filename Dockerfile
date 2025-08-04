# ---- Base image (default fallback) ----
ARG BASE_IMAGE
FROM ${BASE_IMAGE:-openjdk:21-jdk-slim}

# ---- Runtime arguments ----
ARG SERVER_PORT
ARG JAR_FILENAME
ARG JAR_FILE_PATH
ARG CASE_URN_MAPPER_URL
ARG CJSCPPUID

RUN required_variables=" \
    SERVER_PORT  \
    JAR_FILENAME  \
    JAR_FILE_PATH  \
    CASE_URN_MAPPER_URL \
    CJSCPPUID \
" &&  \
    set -- $required_variables && \
    for var in "$@"; do \
      eval val=\$$var; \
      test -n "$val" || { echo "==>> Error!!! Environment variable not set: $var"; exit 1; }; \
    done

ENV JAR_FILENAME=${JAR_FILENAME:-app.jar}
ENV JAR_FILE_PATH=${JAR_FILE_PATH:-build/libs}
ENV JAR_FULL_PATH=$JAR_FILE_PATH/$JAR_FILENAME

ENV CASE_URN_MAPPER_URL=$CASE_URN_MAPPER_URL
ENV CJSCPPUID=$CJSCPPUID

# ---- Set runtime ENV for Spring Boot to bind port
ENV SERVER_PORT=${SERVER_PORT:-4550}

# ---- Dependencies ----
RUN apt-get update \
    && apt-get install -y curl \
    && rm -rf /var/lib/apt/lists/*

# ---- Application files ----
COPY $JAR_FULL_PATH /opt/app/app.jar
COPY lib/applicationinsights.json /opt/app/

# ---- Permissions ----
RUN chmod 755 /opt/app/app.jar

# ---- Runtime ----
EXPOSE 4550

CMD ["java", "-jar", "/opt/app/app.jar"]