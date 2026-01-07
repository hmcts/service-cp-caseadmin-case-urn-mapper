FROM eclipse-temurin:21

WORKDIR /app

# ---- Dependencies ----
RUN apt-get update \
    && apt-get install -y curl \
    && rm -rf /var/lib/apt/lists/*

# ---- Application files ----
COPY build/libs/*.jar /app/
COPY lib/applicationinsights.json /app/

#---Certs---
COPY ${CERTS_DIR}/cpp-nonlive-ca.pem /usr/local/share/ca-certificates/cpp-nonlive-ca.crt
COPY ${CERTS_DIR}/cp-cjs-hmcts-net-ca.pem /usr/local/share/ca-certificates/cp-cjs-hmcts-net-ca.crt
COPY ${CERTS_DIR}/cjscp-nl-root.pem /usr/local/share/ca-certificates/cjscp-nl-root.crt
COPY ${CERTS_DIR}/cjscp-lv-root.pem /usr/local/share/ca-certificates/cjscp-lv-root.crt

RUN update-ca-certificates

RUN keytool -importcert -trustcacerts -cacerts -file /usr/local/share/ca-certificates/cpp-nonlive-ca.crt -alias cpp-nonlive -storepass changeit -noprompt
RUN keytool -importcert -trustcacerts -cacerts -file /usr/local/share/ca-certificates/cp-cjs-hmcts-net-ca.crt -alias cpp-live -storepass changeit -noprompt
RUN keytool -importcert -trustcacerts -cacerts -file /usr/local/share/ca-certificates/cjscp-nl-root.crt -alias cjscp-nonlive -storepass changeit -noprompt
RUN keytool -importcert -trustcacerts -cacerts -file /usr/local/share/ca-certificates/cjscp-lv-root.crt -alias cjscp-live -storepass changeit -noprompt

# ---- Runtime ----
EXPOSE 4550

ENTRYPOINT ["sh","-c","exec java -jar $(ls /app/*.jar | grep -v 'plain' | head -n1)"]