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
                        sanitizeForLog(url),
                        responseEntity.getStatusCode(),
                        sanitizeForLog(truncateForLog(String.valueOf(responseEntity.getBody())))
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

    private String sanitizeForLog(final String input) {
        final StringBuilder sanitized = new StringBuilder();
        if (input != null) {
            for (final char c : input.toCharArray()) {
                switch (c) {
                    case '\n':
                        sanitized.append("\\n");
                        break;
                    case '\r':
                        sanitized.append("\\r");
                        break;
                    case '\t':
                        sanitized.append("\\t");
                        break;
                    default:
                        sanitized.append(c);
                        break;
                }
            }
        }
        return sanitized.toString();
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
