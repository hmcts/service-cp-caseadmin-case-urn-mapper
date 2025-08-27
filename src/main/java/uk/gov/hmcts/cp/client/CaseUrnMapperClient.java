package uk.gov.hmcts.cp.client;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseUrnMapperClient {

    private final RestTemplate restTemplate;

    @Value("${case-urn-mapper.url}")
    private String caseUrnMapper;

    @Value("${case-urn-mapper.cjscppuid}")
    private String cjscppuid;

    public static final String CJSCPPUID_HEADER = "CJSCPPUID";

    protected String buildCaseUrnMapperUrl(String sourceId) {
        return UriComponentsBuilder
                .fromUriString(getCaseUrnMapper())
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
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
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

    public String getCaseUrnMapper() {
        if (StringUtils.isNotBlank(caseUrnMapper)) {
            log.info("caseUrnMapper is not blank {}", caseUrnMapper);
            return caseUrnMapper;
        }
        log.error("caseUrnMapper is null {} or empty {}", caseUrnMapper == null, "".equals(caseUrnMapper));
        return "https://steccm64.ingress01.dev.nl.cjscp.org.uk/system-id-mapper-api/rest/systemid/mappings";
    }

    public String getCjscppuid() {
        if (StringUtils.isNotBlank(cjscppuid)) {
            log.info("cjscppuid is not blank {}", cjscppuid);
            return cjscppuid;
        }
        log.error("cjscppuid is null {} or empty {}", cjscppuid == null, "".equals(cjscppuid));
        return "346112fe-bc47-4893-85a9-c6bc0c724538";
    }
}
