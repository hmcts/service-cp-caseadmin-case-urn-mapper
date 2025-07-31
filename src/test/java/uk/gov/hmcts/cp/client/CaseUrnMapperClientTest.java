package uk.gov.hmcts.cp.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
class CaseUrnMapperClientTest {

    @Autowired
    private CaseUrnMapperClient caseUrnMapperClient;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    void shouldBuildJudgesUrlCorrectly() {
        String sourceId = "CIK2JQKECS";
        String expectedUrl = "https://steccm64.ingress01.dev.nl.cjscp.org.uk/system-id-mapper-api/rest/systemid/mappings?sourceId=CIK2JQKECS&targetType=CASE_FILE_ID";

        String actualUrl = caseUrnMapperClient.buildCaseUrnMapperUrl(sourceId);
        assertThat(actualUrl).isEqualTo(expectedUrl);
    }

    @Test
    void shouldReturnJudgeDetails_whenRequestSucceeds() {
        String sourceId = "CIK2JQKECS";
        String expectedUrl = "https://steccm64.ingress01.dev.nl.cjscp.org.uk/system-id-mapper-api/rest/systemid/mappings?sourceId=CIK2JQKECS&targetType=CASE_FILE_ID";

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
        String sourceId = "CIK2JQKECS";
        String expectedUrl = "https://steccm64.ingress01.dev.nl.cjscp.org.uk/system-id-mapper-api/rest/systemid/mappings?sourceId=CIK2JQKECS&targetType=CASE_FILE_ID";

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