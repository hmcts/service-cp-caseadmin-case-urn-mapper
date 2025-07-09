package uk.gov.hmcts.cp.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;

@Component
@RequiredArgsConstructor
public class InMemoryCaseUrnMapperRepositoryImpl implements CaseUrnMapperRepository {

    private final CaseUrnCacheService cacheService;

    public CaseMapperResponse getCaseIdByCaseUrn(final String caseUrn, final Boolean refresh) {
        if (Boolean.TRUE.equals(refresh)) {
            return cacheService.getCachedCaseIdAndRefreshCache(caseUrn);
        }
        return cacheService.getCachedCaseId(caseUrn);
    }
}
