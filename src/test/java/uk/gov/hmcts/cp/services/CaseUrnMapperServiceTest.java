package uk.gov.hmcts.cp.services;

import org.junit.jupiter.api.Test;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;
import uk.gov.hmcts.cp.repositories.CaseUrnCacheService;
import uk.gov.hmcts.cp.repositories.CaseUrnMapperRepository;
import uk.gov.hmcts.cp.repositories.InMemoryCaseUrnMapperRepositoryImpl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaseUrnMapperServiceTest {

    private final CompositeCacheManager cacheManager = new CompositeCacheManager();
    private final CaseUrnCacheService cacheService = new CaseUrnCacheService(cacheManager);
    private final CaseUrnMapperRepository caseUrnMapperRepository = new InMemoryCaseUrnMapperRepositoryImpl(cacheService);
    private final CaseUrnMapperService caseUrnMapperService = new CaseUrnMapperService(caseUrnMapperRepository);

    @Test
    void shouldReturnStubbedCaseIdResponse_whenValidCaseUrnProvided() {
        String validCaseUrn = "123-ABC-456";

        CaseMapperResponse response = caseUrnMapperService.getCaseIdByCaseUrn(validCaseUrn, true);

        assertEquals(validCaseUrn, response.getCaseUrn());
        assertNotNull(response.getCaseId());
        assertTrue(response.getCaseId().startsWith(validCaseUrn + ":THIS-IS-CASE-ID:"));
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