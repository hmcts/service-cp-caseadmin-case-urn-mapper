package uk.gov.hmcts.cp.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CaseUrnMapperRepositoryTest {

    private CaseUrnCacheService cacheService;
    private CaseUrnMapperRepository caseUrnMapperRepository;

    @BeforeEach
    void setUp() {
        cacheService = mock(CaseUrnCacheService.class);
        caseUrnMapperRepository = new InMemoryCaseUrnMapperRepositoryImpl(cacheService);
    }

    @Test
    void getCaseIdByCaseUrn_shouldCall_getCachedCaseIdAndRefreshCache_ifNoRefresh() {
        String caseUrn = UUID.randomUUID().toString();

        CaseMapperResponse caseMapperResponse = CaseMapperResponse.builder()
                .caseUrn(caseUrn)
                .caseId("mock-case-id")
                .build();
        when(cacheService.getCaseIdAndRefreshCache(caseUrn)).thenReturn(caseMapperResponse);
        when(cacheService.getCachedCaseId(caseUrn)).thenReturn(caseMapperResponse);

        CaseMapperResponse response = caseUrnMapperRepository.getCaseIdByCaseUrn(caseUrn, true);

        assertEquals(caseMapperResponse, response);
        verify(cacheService, times(1)).getCaseIdAndRefreshCache(eq(caseUrn));
        verify(cacheService, times(0)).getCachedCaseId(eq(caseUrn));
    }

    @Test
    void getCaseIdByCaseUrn_shouldCall_getCachedCaseId_ifNoRefresh() {
        String caseUrn = UUID.randomUUID().toString();

        CaseMapperResponse caseMapperResponse = CaseMapperResponse.builder()
                .caseUrn(caseUrn)
                .caseId("mock-case-id")
                .build();
        when(cacheService.getCaseIdAndRefreshCache(caseUrn)).thenReturn(caseMapperResponse);
        when(cacheService.getCachedCaseId(caseUrn)).thenReturn(caseMapperResponse);

        CaseMapperResponse response = caseUrnMapperRepository.getCaseIdByCaseUrn(caseUrn, false);

        assertEquals(caseMapperResponse, response);
        verify(cacheService, times(0)).getCaseIdAndRefreshCache(eq(caseUrn));
        verify(cacheService, times(1)).getCachedCaseId(eq(caseUrn));
    }

}