package uk.gov.hmcts.cp.repositories;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cp.config.CachingConfig;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CaseUrnCacheService {

    private final CacheManager cacheManager;

    @Cacheable(value = CachingConfig.CASE_ID_BY_CASE_URN, key = "#caseUrn")
    public CaseMapperResponse getCachedCaseId(String caseUrn) {
        return getCaseIdByCaseUrnFresh(caseUrn);
    }

    public CaseMapperResponse getCachedCaseIdAndRefreshCache(final String caseUrn) {
        CaseMapperResponse caseMapperResponse = getCaseIdByCaseUrnFresh(caseUrn);
        Cache cache = cacheManager.getCache(CachingConfig.CASE_ID_BY_CASE_URN);
        if (cache != null) {
            cache.put(caseUrn, caseMapperResponse);
        }
        return caseMapperResponse;

    }

    private CaseMapperResponse getCaseIdByCaseUrnFresh(final String caseUrn) {
        final String unescapedCaseUrn = StringEscapeUtils.unescapeHtml4(caseUrn);

        return CaseMapperResponse.builder()
                .caseUrn(unescapedCaseUrn)
                .caseId(unescapedCaseUrn + ":THIS-IS-CASE-ID:" + UUID.randomUUID())
                .build();
    }
}
