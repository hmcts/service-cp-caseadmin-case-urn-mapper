package uk.gov.hmcts.cp.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;
import uk.gov.hmcts.cp.services.CaseUrnMapperService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseUrnMapperControllerTest {

    @Mock
    CaseUrnMapperService caseUrnMapperService;

    @InjectMocks
    CaseUrnMapperController caseUrnMapperController;

    @Test
    void controller_should_validate_case_urn() {
        assertThrows(ResponseStatusException.class, () -> caseUrnMapperController.getCaseIdByCaseUrn(null, true));
        assertThrows(ResponseStatusException.class, () -> caseUrnMapperController.getCaseIdByCaseUrn("", true));
        assertThrows(ResponseStatusException.class, () -> caseUrnMapperController.getCaseIdByCaseUrn("NONEALPHANUM-", true));
        assertThrows(ResponseStatusException.class, () -> caseUrnMapperController.getCaseIdByCaseUrn("NONEALPHANUM ", true));
        assertThrows(ResponseStatusException.class, () -> caseUrnMapperController.getCaseIdByCaseUrn("NONE ALPHANUM", true));
        assertThrows(ResponseStatusException.class, () -> caseUrnMapperController.getCaseIdByCaseUrn("A".repeat(37), true));
        assertThrows(ResponseStatusException.class, () -> caseUrnMapperController.getCaseIdByCaseUrn("A".repeat(35) + "-1", true));
        assertThrows(ResponseStatusException.class, () -> caseUrnMapperController.getCaseIdByCaseUrn("-1", true));
        assertThrows(ResponseStatusException.class, () -> caseUrnMapperController.getCaseIdByCaseUrn("32D90374026-", true));
        assertThrows(ResponseStatusException.class, () -> caseUrnMapperController.getCaseIdByCaseUrn("32D90374026-A", true));
    }

    @Test
    void controller_should_call_service() {
        when(caseUrnMapperService.getCaseIdByCaseUrn("CT98KRYCAP", true)).thenReturn(CaseMapperResponse.builder().build());
        caseUrnMapperController.getCaseIdByCaseUrn("CT98KRYCAP", true);
        verify(caseUrnMapperService).getCaseIdByCaseUrn("CT98KRYCAP", true);
    }

    @Test
    void controller_should_call_service_withHyphenatedSuffix() {
        when(caseUrnMapperService.getCaseIdByCaseUrn("32D90374026-1", true)).thenReturn(CaseMapperResponse.builder().build());
        caseUrnMapperController.getCaseIdByCaseUrn("32D90374026-1", true);
        verify(caseUrnMapperService).getCaseIdByCaseUrn("32D90374026-1", true);
    }

    @Test
    void controller_should_call_service_withMaxLengthCaseUrn() {
        final String maxLengthCaseUrn = "A".repeat(36);
        when(caseUrnMapperService.getCaseIdByCaseUrn(maxLengthCaseUrn, true)).thenReturn(CaseMapperResponse.builder().build());
        caseUrnMapperController.getCaseIdByCaseUrn(maxLengthCaseUrn, true);
        verify(caseUrnMapperService).getCaseIdByCaseUrn(maxLengthCaseUrn, true);
    }

    @Test
    void controller_should_call_service_withMaxLengthHyphenatedCaseUrn() {
        final String maxLengthCaseUrn = "A".repeat(34) + "-1";
        when(caseUrnMapperService.getCaseIdByCaseUrn(maxLengthCaseUrn, true)).thenReturn(CaseMapperResponse.builder().build());
        caseUrnMapperController.getCaseIdByCaseUrn(maxLengthCaseUrn, true);
        verify(caseUrnMapperService).getCaseIdByCaseUrn(maxLengthCaseUrn, true);
    }

    @Test
    void controller_should_default_to_refresh_false() {
        when(caseUrnMapperService.getCaseIdByCaseUrn("DB98KRYCAP", false)).thenReturn(CaseMapperResponse.builder().build());
        caseUrnMapperController.getCaseIdByCaseUrn("DB98KRYCAP", null);
        verify(caseUrnMapperService).getCaseIdByCaseUrn("DB98KRYCAP", false);
    }
}