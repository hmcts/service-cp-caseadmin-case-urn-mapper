package uk.gov.hmcts.cp.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.api.CaseIdByCaseUrnApi;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;
import uk.gov.hmcts.cp.services.CaseUrnMapperService;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CaseUrnMapperController implements CaseIdByCaseUrnApi {

    private final CaseUrnMapperService caseUrnMapperService;

    @Override
    public ResponseEntity<CaseMapperResponse> getCaseIdByCaseUrn(final String caseUrn, final Boolean refreshBoolean) {
        boolean refresh = Boolean.TRUE.equals(refreshBoolean);
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

    private String sanitizeCaseUrn(final String urn) {
        if (urn == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "caseUrn is required");
        }
        return Encode.forJava(urn);
    }
}
