package uk.gov.hmcts.cp.repositories;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;

@Component
public class InMemoryCaseUrnMapperRepositoryImpl implements CaseUrnMapperRepository {

    public CaseMapperResponse getCaseIdByCaseUrn(final String caseUrn) {
        final String unescapedCaseUrn = StringEscapeUtils.unescapeHtml4(caseUrn);
        return CaseMapperResponse.builder()
                .caseUrn(unescapedCaseUrn)
                .caseId(unescapedCaseUrn + "-THIS-IS-CASE-ID")
                .build();
    }
}
