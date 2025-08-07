package uk.gov.hmcts.cp.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.api.CaseIdByCaseUrnApi;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;
import uk.gov.hmcts.cp.services.CaseUrnMapperService;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CaseUrnMapperController implements CaseIdByCaseUrnApi {

    private final CaseUrnMapperService caseUrnMapperService;

    @Override
    public ResponseEntity<CaseMapperResponse> getTest(String caseUrn) {
        try {
            final String sanitizedCaseUrn = sanitizeCaseUrn(caseUrn);
            final CaseMapperResponse caseMapperResponse = caseUrnMapperService.getCaseIdByCaseUrn(sanitizedCaseUrn, false);
            log.debug("Found case ID for caseUrn: {}", sanitizedCaseUrn);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(caseMapperResponse);
        } catch (ResponseStatusException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @Override
    public ResponseEntity<CaseMapperResponse> getCaseIdByCaseUrn(final String caseUrn, final Boolean refresh) {
        try {
            final String sanitizedCaseUrn = sanitizeCaseUrn(caseUrn);
            final CaseMapperResponse caseMapperResponse = caseUrnMapperService.getCaseIdByCaseUrn(sanitizedCaseUrn, refresh);
            log.debug("Found case ID for caseUrn: {}", sanitizedCaseUrn);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(caseMapperResponse);
        } catch (ResponseStatusException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @Override
    public ResponseEntity<CaseMapperResponse> getCaseIdByCaseUrnPost(String caseUrn, Boolean refresh) {
        log.debug("POST request, Found case ID for caseUrn: {}", caseUrn);
        return getCaseIdByCaseUrn(caseUrn, refresh);
    }

    @Override
    public ResponseEntity<CaseMapperResponse> getTestCaseIdByCaseUrn(String caseUrn, Boolean refresh) {
        CaseMapperResponse caseMapperResponse = CaseMapperResponse.builder()
                .caseUrn(caseUrn)
                .caseId(caseUrn + ":This-is-caseId-GET")
                .originalResponse(Map.of("caseUrn", caseUrn))
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(caseMapperResponse);
    }

    @Override
    public ResponseEntity<CaseMapperResponse> getTestCaseIdByCaseUrnPost(String caseUrn, Boolean refresh) {
        CaseMapperResponse caseMapperResponse = CaseMapperResponse.builder()
                .caseUrn(caseUrn)
                .caseId(caseUrn + ":This-is-caseId-POST")
                .originalResponse(Map.of("caseUrn", caseUrn))
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(caseMapperResponse);
    }

    private String sanitizeCaseUrn(final String urn) {
        if (urn == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "caseUrn is required");
        }
        return StringEscapeUtils.escapeHtml4(urn);
    }
}
