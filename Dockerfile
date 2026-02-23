# az login; az acr login -n crmdvrepo01 to authenticate to hmcts Azure Container Registry
FROM crmdvrepo01.azurecr.io/hmcts/apm-services:25-jre

# run as non-root ... group and user "app"
RUN groupadd -r app && useradd -r -g app app
WORKDIR /app

# ---- Dependencies ----
RUN apt-get update \
    && apt-get install -y curl \
    && rm -rf /var/lib/apt/lists/*

# ---- Application files ----
COPY docker/* /app/
COPY build/libs/*.jar /app/
COPY lib/applicationinsights.json /app/

USER app
ENTRYPOINT ["/bin/sh","./startup.sh"]