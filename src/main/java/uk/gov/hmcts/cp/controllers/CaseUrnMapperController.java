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

    private static final String CASE_URN_REGEX = "^[0-9a-zA-Z]{10,40}$";
    private final CaseUrnMapperService caseUrnMapperService;
    private CaseMapperResponse response;

    @Override
    public ResponseEntity<CaseMapperResponse> getCaseIdByCaseUrn(final String caseUrn, final Boolean refreshBoolean) {
        final boolean refresh = Boolean.TRUE.equals(refreshBoolean);
        final CaseMapperResponse response = caseUrnMapperService.getCaseIdByCaseUrn(validateCaseUrn(caseUrn), refresh);
        log.info("Mapped caseUrn:{} to caseId:{}", response.getCaseUrn(), response.getCaseId());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    private String validateCaseUrn(final String caseUrn) {
        if (caseUrn == null || !caseUrn.matches(CASE_URN_REGEX)) {
            log.info("CaseUrn {} does not match expected caseRegex:{}", Encode.forJava(caseUrn), CASE_URN_REGEX);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Case urn must be between 10 and 40 alphanumerics");
        }
        return caseUrn;
    }
}
