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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
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

    protected String buildCaseUrnMapperUrl(final String sourceId) {
        return UriComponentsBuilder
                .fromUriString(getCpBackendUrl())
                .path(getCaseUrnMapperPath())
                .queryParam("sourceId", sourceId)
                .queryParam("targetType", "CASE_FILE_ID")
                .toUriString();
    }

    public ResponseEntity<Object> getCaseFileByCaseUrn(final String sourceId) {
        ResponseEntity<Object> response = null;

        try {
            final String url = buildCaseUrnMapperUrl(sourceId);
            ignoreCertificates();

            final HttpEntity<String> requestEntity = getRequestEntity();
            final ResponseEntity<Object> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Object.class
            );

            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                log.error(
                        "Error while calling System ID Mapper API {}, status {}, body {}",
                        url,
                        responseEntity.getStatusCode(),
                        responseEntity.getBody()
                );
            }
            response = responseEntity;

        } catch (HttpClientErrorException.NotFound notFound) {
            response = ResponseEntity.notFound().build();
        } catch (HttpClientErrorException clientError) {
            log.error("Client error while calling System ID Mapper API", clientError);
        } catch (RestClientException restClientException) {
            log.error("REST error while calling System ID Mapper API", restClientException);
        }

        return response;
    }

    public HttpEntity<String> getRequestEntity() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, "application/vnd.systemid.mapping+json");
        headers.add(CJSCPPUID_HEADER, getCjscppuid());
        return new HttpEntity<>(headers);
    }

    private void ignoreCertificates() {
        final TrustManager trustManager = new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
                // intentionally ignored
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
                // intentionally ignored
            }
        };

        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("Error while ignoring SSL certificate", e);
        }
    }

    public String getCpBackendUrl() {
        String value = null;
        if (StringUtils.isNotBlank(cpBackendUrl)) {
            value = cpBackendUrl;
        } else {
            log.error("cpBackendUrl is null or empty");
        }
        return value;
    }

    public String getCaseUrnMapperPath() {
        String value = null;
        if (StringUtils.isNotBlank(caseUrnMapperPath)) {
            value = caseUrnMapperPath;
        } else {
            log.error("caseUrnMapperPath is null or empty");
        }
        return value;
    }

    public String getCjscppuid() {
        String value = null;
        if (StringUtils.isNotBlank(cjscppuid)) {
            value = cjscppuid;
        } else {
            log.error("cjscppuid is null or empty");
        }
        return value;
    }
}
