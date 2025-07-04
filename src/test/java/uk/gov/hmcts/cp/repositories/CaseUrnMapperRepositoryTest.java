package uk.gov.hmcts.cp.repositories;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CaseUrnMapperRepositoryTest {

    private CaseUrnMapperRepository caseUrnMapperRepository;

    @BeforeEach
    void setUp() {
        caseUrnMapperRepository = new InMemoryCaseUrnMapperRepositoryImpl();
    }

    @Test
    void getCaseIdByCaseUrn_shouldReturnCaseIdResponse() {
        String caseUrn = UUID.randomUUID().toString();
        CaseMapperResponse response = caseUrnMapperRepository.getCaseIdByCaseUrn(caseUrn);

        assertEquals(caseUrn, response.getCaseUrn());
        assertEquals(caseUrn + "-THIS-IS-CASE-ID", response.getCaseId());
    }

    @Test
    void getCaseIdByCaseUrn_shouldReturnCaseIdResponse_unescaped() {
        String caseUrn = "123.c0m$<123>";
        CaseMapperResponse response = caseUrnMapperRepository.getCaseIdByCaseUrn(StringEscapeUtils.escapeHtml4(caseUrn));

        assertEquals(caseUrn, response.getCaseUrn());
        assertEquals(caseUrn + "-THIS-IS-CASE-ID", response.getCaseId());
    }
}