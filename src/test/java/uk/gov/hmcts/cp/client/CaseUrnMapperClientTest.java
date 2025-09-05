package uk.gov.hmcts.cp.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CaseUrnMapperClientTest {

    private CaseUrnMapperClient caseUrnMapperClient;

    private RestTemplate restTemplate;

    private final String url = "http://mock-server";
    private final String path = "/system-id-mapper-api/rest/systemid/mappings";
    private final String cjscppuid = "mock-cjscppuid";

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        caseUrnMapperClient = new CaseUrnMapperClient(restTemplate) {
            @Override
            public String getCpBackendUrl() {
                return url;
            }

            @Override
            public String getCaseUrnMapperPath() {
                return path;
            }

            @Override
            public String getCjscppuid() {
                return cjscppuid;
            }
        };
    }

    @Test
    void shouldBuildJudgesUrlCorrectly() {
        String sourceId = "SOURCE_ID_123";
        String expectedUrl = "http://mock-server/system-id-mapper-api/rest/systemid/mappings?sourceId=SOURCE_ID_123&targetType=CASE_FILE_ID";

        String actualUrl = caseUrnMapperClient.buildCaseUrnMapperUrl(sourceId);
        assertThat(actualUrl).isEqualTo(expectedUrl);
    }

    @Test
    void shouldReturnJudgeDetails_whenRequestSucceeds() {
        String sourceId = "SOURCE_ID_123";
        String expectedUrl = "http://mock-server/system-id-mapper-api/rest/systemid/mappings?sourceId=SOURCE_ID_123&targetType=CASE_FILE_ID";

        Map<String, String> body = Map.of("key 1", "value 1");
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(body, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(caseUrnMapperClient.getRequestEntity()),
                eq(Object.class)
        )).thenReturn(mockResponse);

        ResponseEntity<Object> response = caseUrnMapperClient.getCaseFileByCaseUrn(sourceId);

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isEqualTo(body);
    }

    @Test
    void shouldReturnNull_whenRestTemplateThrowsException() {
        String sourceId = "SOURCE_ID_123";
        String expectedUrl = "http://mock-server/system-id-mapper-api/rest/systemid/mappings?sourceId=SOURCE_ID_123&targetType=CASE_FILE_ID";

        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(caseUrnMapperClient.getRequestEntity()),
                eq(String.class)
        )).thenThrow(new RestClientException("Connection error"));

        ResponseEntity<Object> response = caseUrnMapperClient.getCaseFileByCaseUrn(sourceId);

        assertThat(response).isNull();
    }
}