package com.woorifisa.won_invest_channel_server.domain.invest.service;

import com.woorifisa.won_invest_channel_server.domain.account.model.AccountStatus;
import com.woorifisa.won_invest_channel_server.domain.account.model.InvestChnAccountSummary;
import com.woorifisa.won_invest_channel_server.domain.account.repository.InvestChnAccountSummaryRepository;
import com.woorifisa.won_invest_channel_server.domain.invest.dto.response.InvestEtfHoldingsResponse;
import com.woorifisa.won_invest_channel_server.domain.invest.exception.code.InvestErrorCode;
import com.woorifisa.won_invest_channel_server.domain.invest.external.InvestCoreEtfQueryApi;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import feign.FeignException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestEtfQueryService {

    private final InvestChnAccountSummaryRepository accountSummaryRepository;
    private final InvestCoreEtfQueryApi investCoreEtfQueryApi;

    public InvestEtfHoldingsResponse getAccountEtfs(UUID userUuid, UUID accountUuid) {
        InvestChnAccountSummary account = accountSummaryRepository.findById(accountUuid)
                .orElseThrow(() -> new BusinessException(InvestErrorCode.ACCOUNT_NOT_FOUND));

        validateOwnership(userUuid, account);
        validateAccountStatus(account);

        return fetchCoreEtfHoldings(userUuid, accountUuid);
    }

    private void validateOwnership(UUID userUuid, InvestChnAccountSummary account) {
        if (!account.getUserUuid().equals(userUuid)) {
            throw new BusinessException(InvestErrorCode.ACCOUNT_NOT_OWNER);
        }
    }

    private void validateAccountStatus(InvestChnAccountSummary account) {
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(InvestErrorCode.INVALID_ACCOUNT_STATUS);
        }
    }

    private InvestEtfHoldingsResponse fetchCoreEtfHoldings(UUID userUuid, UUID accountUuid) {
        ApiResponse<InvestEtfHoldingsResponse> coreResponse;
        try {
            coreResponse = investCoreEtfQueryApi.getAccountEtfHoldings(userUuid, accountUuid);
        } catch (FeignException e) {
            log.warn("Invest Core ETF holdings query failed [status={}]", e.status());
            throw new BusinessException(InvestErrorCode.INTERNAL_QUERY_FAILED);
        }

        if (coreResponse == null || coreResponse.status() != 200 || coreResponse.data() == null) {
            log.warn("Invest Core ETF holdings response invalid [status={}, code={}]",
                    coreResponse == null ? null : coreResponse.status(),
                    coreResponse == null ? null : coreResponse.code());
            throw new BusinessException(InvestErrorCode.INTERNAL_QUERY_FAILED);
        }

        return coreResponse.data();
    }
}
