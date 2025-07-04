package uk.gov.hmcts.cp.services;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;
import uk.gov.hmcts.cp.repositories.CaseUrnMapperRepository;
import uk.gov.hmcts.cp.repositories.InMemoryCaseUrnMapperRepositoryImpl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CaseUrnMapperServiceTest {

    private final CaseUrnMapperRepository caseUrnMapperRepository = new InMemoryCaseUrnMapperRepositoryImpl();
    private final CaseUrnMapperService caseUrnMapperService = new CaseUrnMapperService(caseUrnMapperRepository);

    @Test
    void shouldReturnStubbedCaseIdResponse_whenValidCaseUrnProvided() {
        // Arrange
        String validCaseUrn = "123-ABC-456";

        // Act
        CaseMapperResponse response = caseUrnMapperService.getCaseIdByCaseUrn(validCaseUrn);

        // Assert
        assertEquals(validCaseUrn, response.getCaseUrn());
        assertEquals(validCaseUrn + "-THIS-IS-CASE-ID", response.getCaseId());
    }

    @Test
    void shouldThrowBadRequestException_whenCaseUrnIsNull() {
        // Arrange
        String nullCaseUrn = null;

        // Act & Assert
        assertThatThrownBy(() -> caseUrnMapperService.getCaseIdByCaseUrn(nullCaseUrn)).isInstanceOf(ResponseStatusException.class).hasMessageContaining("400 BAD_REQUEST").hasMessageContaining("caseUrn is required");
    }

    @Test
    void shouldThrowBadRequestException_whenCaseUrnIsEmpty() {
        // Arrange
        String emptyCaseUrn = "";

        // Act & Assert
        assertThatThrownBy(() -> caseUrnMapperService.getCaseIdByCaseUrn(emptyCaseUrn)).isInstanceOf(ResponseStatusException.class).hasMessageContaining("400 BAD_REQUEST").hasMessageContaining("caseUrn is required");
    }
}