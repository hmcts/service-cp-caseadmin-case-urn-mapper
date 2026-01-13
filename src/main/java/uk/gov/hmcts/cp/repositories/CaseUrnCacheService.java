package uk.gov.hmcts.cp.repositories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cp.client.CaseUrnMapperClient;
import uk.gov.hmcts.cp.client.UrnMapperResponse;
import uk.gov.hmcts.cp.config.CachingConfig;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseUrnCacheService {

    public static final String SOURCE_ID = "sourceId";
    public static final String TARGET_ID = "targetId";

    private final CaseUrnMapperClient caseUrnMapperClient;
    private final CacheManager cacheManager;

    @Cacheable(value = CachingConfig.CASE_ID_BY_CASE_URN, key = "#caseUrn")
    public CaseMapperResponse getCachedCaseId(final String caseUrn) {
        return getCaseIdByCaseUrnFresh(caseUrn);
    }

    public CaseMapperResponse getCaseIdAndRefreshCache(final String caseUrn) {
        final CaseMapperResponse caseMapperResponse = getCaseIdByCaseUrnFresh(caseUrn);
        final Cache cache = cacheManager.getCache(CachingConfig.CASE_ID_BY_CASE_URN);

        if (cache != null) {
            cache.put(caseUrn, caseMapperResponse);
        }

        return caseMapperResponse;
    }

    private CaseMapperResponse getCaseIdByCaseUrnFresh(final String caseUrn) {
        final String unescapedCaseUrn = Encode.forJava(caseUrn);
        final ResponseEntity<UrnMapperResponse> responseEntity = caseUrnMapperClient.getCaseFileByCaseUrn(unescapedCaseUrn);
        return CaseMapperResponse.builder()
                .caseUrn(responseEntity.getBody().getSourceId())
                .caseId(responseEntity.getBody().getTargetId())
                .build();
    }
}
