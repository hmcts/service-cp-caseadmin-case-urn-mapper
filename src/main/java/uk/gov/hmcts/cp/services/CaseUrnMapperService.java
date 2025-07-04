package uk.gov.hmcts.cp.services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.model.CaseMapperResponse;
import uk.gov.hmcts.cp.repositories.CaseUrnMapperRepository;

@Service
@RequiredArgsConstructor
public class CaseUrnMapperService {

    private static final Logger LOG = LoggerFactory.getLogger(CaseUrnMapperService.class);

    private final CaseUrnMapperRepository caseUrnMapperRepository;

    public CaseMapperResponse getCaseIdByCaseUrn(final String caseUrn) throws ResponseStatusException {
        if (StringUtils.isEmpty(caseUrn)) {
            LOG.atWarn().log("No case urn provided");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "caseUrn is required");
        }
        LOG.atWarn().log("NOTE: System configured to return stubbed Case ID details. Ignoring provided caseUrn : {}", caseUrn);
        final CaseMapperResponse caseMapperResponse = caseUrnMapperRepository.getCaseIdByCaseUrn(caseUrn);
        LOG.atDebug().log("Case Mapper response: {}", caseMapperResponse);
        return caseMapperResponse;
    }

}
