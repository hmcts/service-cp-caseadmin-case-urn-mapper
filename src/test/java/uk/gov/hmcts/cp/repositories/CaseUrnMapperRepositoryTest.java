package uk.gov.hmcts.cp.repositories;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.support.CompositeCacheManager;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaseUrnMapperRepositoryTest {

    private CaseUrnMapperRepository caseUrnMapperRepository;

    @BeforeEach
    void setUp() {
        CompositeCacheManager cacheManager = new CompositeCacheManager();
        CaseUrnCacheService cacheService = new CaseUrnCacheService(cacheManager);
        caseUrnMapperRepository = new InMemoryCaseUrnMapperRepositoryImpl(cacheService);
    }

    @Test
    void getCaseIdByCaseUrn_shouldReturnCaseIdResponse() {
        String caseUrn = UUID.randomUUID().toString();
        CaseMapperResponse response = caseUrnMapperRepository.getCaseIdByCaseUrn(caseUrn, true);

        assertEquals(caseUrn, response.getCaseUrn());
        assertNotNull(response.getCaseId());
        assertTrue(response.getCaseId().startsWith(caseUrn + ":THIS-IS-CASE-ID:"));
    }

    @Test
    void getCaseIdByCaseUrn_shouldReturnCaseIdResponse_unescaped() {
        String caseUrn = "123.c0m$<123>";
        CaseMapperResponse response = caseUrnMapperRepository.getCaseIdByCaseUrn(StringEscapeUtils.escapeHtml4(caseUrn), true);

        assertEquals(caseUrn, response.getCaseUrn());
        assertNotNull(response.getCaseId());
        assertTrue(response.getCaseId().startsWith(caseUrn + ":THIS-IS-CASE-ID:"));
    }
}