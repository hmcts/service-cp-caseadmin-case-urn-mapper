package uk.gov.hmcts.cp.client;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
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

    @SuppressWarnings("PMD.OnlyOneReturn")
    public ResponseEntity<Object> getCaseFileByCaseUrn(final String sourceId) {
        try {
            final String url = buildCaseUrnMapperUrl(sourceId);
            log.info("get caseId from:{}", url);
            final HttpEntity<String> request = getRequestEntity();
            final ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Object.class
            );
            log.info("get caseId response {}", response.getStatusCode());
            if (response != null && !response.getStatusCode().is2xxSuccessful()) {
                log.error(
                        "get caseId error while calling System ID Mapper API {}, status {}, body {}",
                        Encode.forJava(url),
                        response.getStatusCode(),
                        truncateForLog(String.valueOf(response.getBody()))
                );
            }
            return response;
        } catch (HttpClientErrorException.NotFound notFound) {
            return ResponseEntity.notFound().build();
        } catch (HttpClientErrorException clientError) {
            log.error("Client error while calling System ID Mapper API", clientError);
            return ResponseEntity.status(503).build();
        } catch (RestClientException restClientException) {
            log.error("REST error while calling System ID Mapper API", restClientException);
            return ResponseEntity.status(503).build();
        }
    }

    public HttpEntity<String> getRequestEntity() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, "application/vnd.systemid.mapping+json");
        headers.add(CJSCPPUID_HEADER, getCjscppuid());
        return new HttpEntity<>(headers);
    }

    private String truncateForLog(final String input) {
        final int maxLength = 200;
        final String truncated;
        if (input != null && input.length() > maxLength) {
            truncated = input.substring(0, maxLength) + "...";
        } else {
            truncated = input;
        }
        return truncated;
    }

    public String getCpBackendUrl() {
        final String value;
        if (StringUtils.isNotBlank(cpBackendUrl)) {
            value = cpBackendUrl;
        } else {
            log.error("cpBackendUrl is null or empty");
            value = null;
        }
        return value;
    }

    public String getCaseUrnMapperPath() {
        final String value;
        if (StringUtils.isNotBlank(caseUrnMapperPath)) {
            value = caseUrnMapperPath;
        } else {
            log.error("caseUrnMapperPath is null or empty");
            value = null;
        }
        return value;
    }

    public String getCjscppuid() {
        final String value;
        if (StringUtils.isNotBlank(cjscppuid)) {
            value = cjscppuid;
        } else {
            log.error("cjscppuid is null or empty");
            value = null;
        }
        return value;
    }
}
