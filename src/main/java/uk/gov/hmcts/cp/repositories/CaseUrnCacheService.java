package uk.gov.hmcts.cp.repositories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.client.CaseUrnMapperClient;
import uk.gov.hmcts.cp.config.CachingConfig;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseUrnCacheService {

    public static final String SOURCE_ID = "sourceId";
    public static final String TARGET_ID = "targetId";
    private final CaseUrnMapperClient caseUrnMapperClient;
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

        ResponseEntity<Object> responseEntity = caseUrnMapperClient.getCaseFileByCaseUrn(unescapedCaseUrn);

        if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {

            //  Example response:
            //  {
            //     "mappingId": "xxxxxxxx-2e6b-4171-9a6e-xxxxxxxxxxxx",
            //     "sourceId": "XXXXX12345",
            //     "sourceType": "OU_URN",
            //     "targetId": "xxxxxxxx-f092-415b-839c-xxxxxxxxxxxx",
            //     "targetType": "CASE_FILE_ID",
            //     "createdAt": "2025-07-21T13:19:53.546Z"
            //  }

            Object body = responseEntity.getBody();
            if (body instanceof Map<?, ?> mapBody) {
                if (mapBody.containsKey(SOURCE_ID) && mapBody.containsKey(TARGET_ID)) {
                    final String sourceId = (String) mapBody.get(SOURCE_ID);
                    final String targetId = (String) mapBody.get(TARGET_ID);
                    return CaseMapperResponse.builder()
                            .caseUrn(sourceId)
                            .caseId(targetId)
                            .build();
                }
            }
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Case not found by urn: " + unescapedCaseUrn);
    }
}
