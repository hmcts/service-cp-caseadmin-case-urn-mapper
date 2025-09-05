package uk.gov.hmcts.cp.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.client.CaseUrnMapperClient;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;
import uk.gov.hmcts.cp.repositories.CaseUrnCacheService;
import uk.gov.hmcts.cp.repositories.CaseUrnMapperRepository;
import uk.gov.hmcts.cp.repositories.InMemoryCaseUrnMapperRepositoryImpl;
import uk.gov.hmcts.cp.services.CaseUrnMapperService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CaseUrnMapperControllerTest {

    private CaseUrnMapperRepository caseUrnMapperRepository;
    private CaseUrnMapperService caseUrnMapperService;
    private CaseUrnMapperController caseUrnMapperController;
    private RestTemplate restTemplate;
    private CaseUrnMapperClient caseUrnMapperClient;

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
        CompositeCacheManager cacheManager = new CompositeCacheManager();
        CaseUrnCacheService cacheService = new CaseUrnCacheService(caseUrnMapperClient, cacheManager);
        caseUrnMapperRepository = new InMemoryCaseUrnMapperRepositoryImpl(cacheService);
        caseUrnMapperService = new CaseUrnMapperService(caseUrnMapperRepository);
        caseUrnMapperController = new CaseUrnMapperController(caseUrnMapperService);
    }

    @Test
    void getJudgeById_ShouldReturnJudgesWithOkStatus() {
        String caseUrn = "mock-case-urn";
        String targetId = "mock-target-id";

        Map<String, String> body = Map.of(
                "sourceId", caseUrn,
                "targetId", targetId,
                "some-more-data", "value-1"
        );
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(body, HttpStatus.OK);


        when(restTemplate.exchange(
                eq(url + path + "?sourceId=" + caseUrn + "&targetType=CASE_FILE_ID"),
                eq(HttpMethod.GET),
                eq(caseUrnMapperClient.getRequestEntity()),
                eq(Object.class)
        )).thenReturn(mockResponse);

        ResponseEntity<CaseMapperResponse> response = caseUrnMapperController.getCaseIdByCaseUrn(caseUrn, true);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        CaseMapperResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(caseUrn, responseBody.getCaseUrn());
        assertEquals(targetId, responseBody.getCaseId());
    }

    @Test
    void getJudgeById_ShouldReturnBadRequestStatus() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> caseUrnMapperController.getCaseIdByCaseUrn(null, true));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("caseUrn is required");
        assertThat(exception.getMessage()).isEqualTo("400 BAD_REQUEST \"caseUrn is required\"");
    }

} 