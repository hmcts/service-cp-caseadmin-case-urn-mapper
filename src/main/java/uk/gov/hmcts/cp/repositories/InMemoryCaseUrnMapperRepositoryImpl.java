package uk.gov.hmcts.cp.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;

// we will remove this later as we do not use pact test atm
@Component
@RequiredArgsConstructor
public class InMemoryCaseUrnMapperRepositoryImpl implements CaseUrnMapperRepository {

    private final CaseUrnCacheService cacheService;

    @Override
    public CaseMapperResponse getCaseIdByCaseUrn(final String caseUrn, final Boolean refresh) {
        return Boolean.TRUE.equals(refresh)
                ? cacheService.getCachedCaseIdAndRefreshCache(caseUrn)
                : cacheService.getCachedCaseId(caseUrn);
    }
}
