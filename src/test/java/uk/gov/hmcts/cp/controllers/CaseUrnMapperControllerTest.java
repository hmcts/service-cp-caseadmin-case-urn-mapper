package uk.gov.hmcts.cp.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;
import uk.gov.hmcts.cp.repositories.CaseUrnMapperRepository;
import uk.gov.hmcts.cp.repositories.InMemoryCaseUrnMapperRepositoryImpl;
import uk.gov.hmcts.cp.services.CaseUrnMapperService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CaseUrnMapperControllerTest {

    private CaseUrnMapperRepository caseUrnMapperRepository;
    private CaseUrnMapperService caseUrnMapperService;
    private CaseUrnMapperController caseUrnMapperController;

    @BeforeEach
    void setUp() {
        caseUrnMapperRepository = new InMemoryCaseUrnMapperRepositoryImpl();
        caseUrnMapperService = new CaseUrnMapperService(caseUrnMapperRepository);
        caseUrnMapperController = new CaseUrnMapperController(caseUrnMapperService);
    }

    @Test
    void getJudgeById_ShouldReturnJudgesWithOkStatus() {
        String caseUrn = UUID.randomUUID().toString();
        ResponseEntity<CaseMapperResponse> response = caseUrnMapperController.getCaseIdByCaseUrn(caseUrn);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        CaseMapperResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(caseUrn, responseBody.getCaseUrn());
        assertEquals(caseUrn + "-THIS-IS-CASE-ID", responseBody.getCaseId());
    }

    @Test
    void getCaseIdByCaseUrn_ShouldSanitizeCaseUrn() {
        String unsanitizedCaseUrn = "<script>alert('xss')</script>";

        ResponseEntity<?> response = caseUrnMapperController.getCaseIdByCaseUrn(unsanitizedCaseUrn);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getJudgeById_ShouldReturnBadRequestStatus() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> caseUrnMapperController.getCaseIdByCaseUrn(null));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("caseUrn is required");
        assertThat(exception.getMessage()).isEqualTo("400 BAD_REQUEST \"caseUrn is required\"");
    }

} 