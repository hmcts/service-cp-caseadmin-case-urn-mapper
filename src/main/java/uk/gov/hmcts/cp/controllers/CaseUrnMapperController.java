package uk.gov.hmcts.cp.controllers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.api.CaseIdByCaseUrnApi;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;
import uk.gov.hmcts.cp.services.CaseUrnMapperService;

@RestController
@RequiredArgsConstructor
public class CaseUrnMapperController implements CaseIdByCaseUrnApi {

    private static final Logger LOG = LoggerFactory.getLogger(CaseUrnMapperController.class);

    private final CaseUrnMapperService caseUrnMapperService;

    @Override
    public ResponseEntity<CaseMapperResponse> getCaseIdByCaseUrn(final String caseUrn) {
        try {
            final String sanitizedCaseUrn = sanitizeCaseUrn(caseUrn);
            final CaseMapperResponse caseMapperResponse = caseUrnMapperService.getCaseIdByCaseUrn(sanitizedCaseUrn);
            LOG.atDebug().log("Found case ID for caseUrn: {}", sanitizedCaseUrn);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(caseMapperResponse);
        } catch (ResponseStatusException e) {
            LOG.atError().log(e.getMessage());
            throw e;
        }
    }

    private String sanitizeCaseUrn(final String urn) {
        if (urn == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "caseUrn is required");
        }
        return StringEscapeUtils.escapeHtml4(urn);
    }
}
