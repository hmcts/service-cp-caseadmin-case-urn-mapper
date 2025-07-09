package uk.gov.hmcts.cp.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;
import uk.gov.hmcts.cp.repositories.CaseUrnCacheService;
import uk.gov.hmcts.cp.repositories.CaseUrnMapperRepository;
import uk.gov.hmcts.cp.repositories.InMemoryCaseUrnMapperRepositoryImpl;
import uk.gov.hmcts.cp.services.CaseUrnMapperService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaseUrnMapperControllerTest {

    private CaseUrnMapperRepository caseUrnMapperRepository;
    private CaseUrnMapperService caseUrnMapperService;
    private CaseUrnMapperController caseUrnMapperController;

    @BeforeEach
    void setUp() {
        CompositeCacheManager cacheManager = new CompositeCacheManager();
        CaseUrnCacheService cacheService = new CaseUrnCacheService(cacheManager);
        caseUrnMapperRepository = new InMemoryCaseUrnMapperRepositoryImpl(cacheService);
        caseUrnMapperService = new CaseUrnMapperService(caseUrnMapperRepository);
        caseUrnMapperController = new CaseUrnMapperController(caseUrnMapperService);
    }

    @Test
    void getJudgeById_ShouldReturnJudgesWithOkStatus() {
        String caseUrn = UUID.randomUUID().toString();
        ResponseEntity<CaseMapperResponse> response = caseUrnMapperController.getCaseIdByCaseUrn(caseUrn, true);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        CaseMapperResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(caseUrn, responseBody.getCaseUrn());
        assertNotNull(responseBody.getCaseId());
        assertTrue(responseBody.getCaseId().startsWith(caseUrn + ":THIS-IS-CASE-ID:"));
    }

    @Test
    void getCaseIdByCaseUrn_ShouldEncodeCaseUrn() {
        String unsanitizedCaseUrn = "<script>alert('xss')</script>";

        ResponseEntity<?> response = caseUrnMapperController.getCaseIdByCaseUrn(unsanitizedCaseUrn, true);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
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