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

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CaseUrnMapperController implements CaseIdByCaseUrnApi {

    private static final Logger LOG = LoggerFactory.getLogger(CaseUrnMapperController.class);

    private final CaseUrnMapperService caseUrnMapperService;

    @Override
    public ResponseEntity<CaseMapperResponse> getTest() {
        CaseMapperResponse caseMapperResponse = CaseMapperResponse.builder()
                .caseUrn("this-is-test-case-urn")
                .caseId("this-is-test-case-id")
                .originalResponse(Map.of("test", "this-is-test"))
                .build();
        return ResponseEntity.ok(caseMapperResponse);
    }

    @Override
    public ResponseEntity<CaseMapperResponse> getCaseIdByCaseUrn(final String caseUrn, final Boolean refresh) {
        try {
            final String sanitizedCaseUrn = sanitizeCaseUrn(caseUrn);
            final CaseMapperResponse caseMapperResponse = caseUrnMapperService.getCaseIdByCaseUrn(sanitizedCaseUrn, refresh);
            LOG.atDebug().log("Found case ID for caseUrn: {}", sanitizedCaseUrn);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(caseMapperResponse);
        } catch (ResponseStatusException e) {
            LOG.atError().log(e.getMessage());
            throw e;
        }
    }

    @Override
    public ResponseEntity<CaseMapperResponse> getCaseIdByCaseUrnPost(String caseUrn, Boolean refresh) {
        LOG.atDebug().log("POST request, Found case ID for caseUrn: {}", caseUrn);
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
