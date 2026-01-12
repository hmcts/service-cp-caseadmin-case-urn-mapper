package uk.gov.hmcts.cp.client;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class CaseUrnMapperClient {

    public static final String CJSCPPUID_HEADER = "CJSCPPUID";

    private final RestTemplate restTemplate;
    private final String cpBackendUrl;
    private final String caseUrnMapperPath;
    private final String cjscppuid;

    public CaseUrnMapperClient(final RestTemplate restTemplate,
                               @Value("${case-urn-mapper.url}") final String cpBackendUrl,
                               @Value("${case-urn-mapper.path}") final String caseUrnMapperPath,
                               @Value("${case-urn-mapper.cjscppuid}") final String cjscppuid) {
        this.restTemplate = restTemplate;
        this.cpBackendUrl = cpBackendUrl;
        this.caseUrnMapperPath = caseUrnMapperPath;
        this.cjscppuid = cjscppuid;
    }

    protected String buildCaseUrnMapperUrl(final String sourceId) {
        return UriComponentsBuilder
                .fromUriString(getCpBackendUrl())
                .path(getCaseUrnMapperPath())
                .queryParam("sourceId", sourceId)
                .queryParam("targetType", "CASE_FILE_ID")
                .toUriString();
    }

    public ResponseEntity<Object> getCaseFileByCaseUrn(final String sourceId) {
        final String url = buildCaseUrnMapperUrl(sourceId);
        final HttpEntity<String> requestEntity = getRequestEntity();
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                Object.class
        );
    }


    public HttpEntity<String> getRequestEntity() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, "application/vnd.systemid.mapping+json");
        headers.add(CJSCPPUID_HEADER, getCjscppuid());
        return new HttpEntity<>(headers);
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
