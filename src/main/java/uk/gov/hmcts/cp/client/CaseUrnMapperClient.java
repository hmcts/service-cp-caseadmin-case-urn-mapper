package uk.gov.hmcts.cp.client;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseUrnMapperClient {

    private final RestTemplate restTemplate;

    @Value("${case-urn-mapper.url}")
    private String cpBackendUrl;

    @Value("${case-urn-mapper.path}")
    private String caseUrnMapperPath;

    @Value("${case-urn-mapper.cjscppuid}")
    private String cjscppuid;

    public static final String CJSCPPUID_HEADER = "CJSCPPUID";

    protected String buildCaseUrnMapperUrl(String sourceId) {
        return UriComponentsBuilder
                .fromUriString(getCpBackendUrl())
                .path(getCaseUrnMapperPath())
                .queryParam("sourceId", sourceId)
                .queryParam("targetType", "CASE_FILE_ID")
                .toUriString();
    }

    public ResponseEntity<Object> getCaseFileByCaseUrn(String sourceId) {
        try {
            final String url = buildCaseUrnMapperUrl(sourceId);
            log.warn("Mapper API url {}", url);
            ignoreCertificates();
            HttpEntity<String> requestEntity = getRequestEntity();
            log.warn("Mapper API headers {}", requestEntity.toString());
            ResponseEntity<Object> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Object.class
            );
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                log.error("Error while calling System ID Mapper API {}, Response status {}, body {}", url, responseEntity.getStatusCode(), responseEntity.getBody());
            }
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while calling System ID Mapper API", e);
            if (e instanceof HttpClientErrorException.NotFound) {
                return ResponseEntity.notFound().build();
            }
        }
        return null;
    }

    public HttpEntity<String> getRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, "application/vnd.systemid.mapping+json");
        headers.add(CJSCPPUID_HEADER, getCjscppuid());
        return new HttpEntity<>(headers);
    }

    private void ignoreCertificates() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            log.error("Error while ignoring SSL certificate", e);
        }
    }

    public String getCpBackendUrl() {
        log.error("caseUrnMapper value {}", cpBackendUrl);
        if (StringUtils.isNotBlank(cpBackendUrl)) {
            log.info("caseUrnMapper is not blank {}", cpBackendUrl);
            return cpBackendUrl;
        }
        log.error("caseUrnMapper is null {} or empty {}", cpBackendUrl == null, "".equals(cpBackendUrl));
        return null;
    }

    public String getCaseUrnMapperPath() {
        log.error("caseUrnMapperPath value {}", caseUrnMapperPath);
        if (StringUtils.isNotBlank(caseUrnMapperPath)) {
            log.info("caseUrnMapperPath is not blank {}", caseUrnMapperPath);
            return caseUrnMapperPath;
        }
        log.error("caseUrnMapperPath is null {} or empty {}", caseUrnMapperPath == null, "".equals(caseUrnMapperPath));
        return null;
    }

    public String getCjscppuid() {
        log.error("cjscppuid value {}", cjscppuid);
        if (StringUtils.isNotBlank(cjscppuid)) {
            log.info("cjscppuid is not blank {}", cjscppuid);
            return cjscppuid;
        }
        log.error("cjscppuid is null {} or empty {}", cjscppuid == null, "".equals(cjscppuid));
        return null;
    }
}
