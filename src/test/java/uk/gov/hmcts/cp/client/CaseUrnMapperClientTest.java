package uk.gov.hmcts.cp.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseUrnMapperClientTest {

    @Mock
    private RestTemplate restTemplate;

    private CaseUrnMapperClient caseUrnMapperClient;

    private final String url = "http://mock-server";
    private final String path = "/system-id-mapper-api/rest/systemid/mappings";
    private final String cjscppuid = "mock-cjscppuid";
    private final String sourceId = "SOURCE_ID_123";
    private final String expectedUrl = String.format("%s%s?sourceId=SOURCE_ID_123&targetType=CASE_FILE_ID", url, path);

    @BeforeEach
    void beforeEach() {
        caseUrnMapperClient = new CaseUrnMapperClient(restTemplate, url, path, cjscppuid);
    }

    @Test
    void shouldBuildJudgesUrlCorrectly() {
        String actualUrl = caseUrnMapperClient.buildCaseUrnMapperUrl(sourceId);
        assertThat(actualUrl).isEqualTo(expectedUrl);
    }

    @Test
    void shouldReturnJudgeDetails_whenRequestSucceeds() {
        UrnMapperResponse urnMapperResponse = UrnMapperResponse.builder().sourceId("urn").targetId("guid").build();
        ResponseEntity<UrnMapperResponse> mockResponse = new ResponseEntity<>(urnMapperResponse, HttpStatus.OK);
        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(caseUrnMapperClient.getRequestEntity()),
                eq(UrnMapperResponse.class)
        )).thenReturn(mockResponse);

        ResponseEntity<UrnMapperResponse> response = caseUrnMapperClient.getCaseFileByCaseUrn(sourceId);

        assertThat(response.getBody().getSourceId()).isEqualTo("urn");
        assertThat(response.getBody().getTargetId()).isEqualTo("guid");
    }
}