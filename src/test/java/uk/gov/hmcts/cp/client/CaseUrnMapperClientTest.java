package uk.gov.hmcts.cp.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cp.config.AppProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseUrnMapperClientTest {

    @Mock
    AppProperties appProperties;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CaseUrnMapperClient caseUrnMapperClient;

    private final String url = "http://mock-server";
    private final String path = "/system-id-mapper-api/rest/systemid/mappings";
    private final String cjscppuid = "mock-cjscppuid";
    private final String sourceId = "SOURCE1234";
    private final String expectedUrl = String.format("%s%s?sourceId=SOURCE1234&targetType=CASE_FILE_ID", url, path);

    @Test
    void url_should_build_correctly() {
        when(appProperties.getBackendUrl()).thenReturn(url);
        when(appProperties.getBackendPath()).thenReturn(path);
        String actualUrl = caseUrnMapperClient.buildCaseUrnMapperUrl(sourceId);
        assertThat(actualUrl).isEqualTo(expectedUrl);
    }

    @Test
    void case_id_should_be_returned_ok() {
        when(appProperties.getBackendUrl()).thenReturn(url);
        when(appProperties.getBackendPath()).thenReturn(path);
        UrnMapperResponse urnMapperResponse = UrnMapperResponse.builder().sourceId("urn").targetId("guid").build();
        when(appProperties.getBackendCjscppuid()).thenReturn(cjscppuid);
        HttpEntity<String> requestEntity = caseUrnMapperClient.getRequestEntity();
        ResponseEntity<UrnMapperResponse> mockResponse = new ResponseEntity<>(urnMapperResponse, HttpStatus.OK);
        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(requestEntity),
                eq(UrnMapperResponse.class)
        )).thenReturn(mockResponse);

        ResponseEntity<UrnMapperResponse> response = caseUrnMapperClient.getCaseFileByCaseUrn(sourceId);

        assertThat(response.getBody().getSourceId()).isEqualTo("urn");
        assertThat(response.getBody().getTargetId()).isEqualTo("guid");
    }
}