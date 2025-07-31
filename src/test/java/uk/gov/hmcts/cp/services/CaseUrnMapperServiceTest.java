package uk.gov.hmcts.cp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;
import uk.gov.hmcts.cp.repositories.CaseUrnMapperRepository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CaseUrnMapperServiceTest {

    private CaseUrnMapperRepository caseUrnMapperRepository;
    private CaseUrnMapperService caseUrnMapperService;

    @BeforeEach
    void setUp() {
        caseUrnMapperRepository = mock(CaseUrnMapperRepository.class);
        caseUrnMapperService = new CaseUrnMapperService(caseUrnMapperRepository);
    }

    @Test
    void shouldReturnStubbedCaseIdResponse_whenValidCaseUrnProvided() {
        String caseUrn = "123-ABC-456";

        CaseMapperResponse caseMapperResponse = CaseMapperResponse.builder()
                .caseUrn(caseUrn)
                .caseId("mock-case-id")
                .build();

        when(caseUrnMapperRepository.getCaseIdByCaseUrn(caseUrn, true)).thenReturn(caseMapperResponse);

        CaseMapperResponse response = caseUrnMapperService.getCaseIdByCaseUrn(caseUrn, true);

        assertEquals(caseMapperResponse, response);
    }

    @Test
    void shouldThrowBadRequestException_whenCaseUrnIsNull() {
        String nullCaseUrn = null;

        assertThatThrownBy(() -> caseUrnMapperService.getCaseIdByCaseUrn(nullCaseUrn, true)).isInstanceOf(ResponseStatusException.class).hasMessageContaining("400 BAD_REQUEST").hasMessageContaining("caseUrn is required");
    }

    @Test
    void shouldThrowBadRequestException_whenCaseUrnIsEmpty() {
        String emptyCaseUrn = "";

        assertThatThrownBy(() -> caseUrnMapperService.getCaseIdByCaseUrn(emptyCaseUrn, true)).isInstanceOf(ResponseStatusException.class).hasMessageContaining("400 BAD_REQUEST").hasMessageContaining("caseUrn is required");
    }
}