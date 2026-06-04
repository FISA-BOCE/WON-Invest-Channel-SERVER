package com.woorifisa.won_invest_channel_server.domain.invest.service;

import com.woorifisa.won_invest_channel_server.domain.account.model.AccountStatus;
import com.woorifisa.won_invest_channel_server.domain.account.model.InvestChnAccountSummary;
import com.woorifisa.won_invest_channel_server.domain.account.repository.InvestChnAccountSummaryRepository;
import com.woorifisa.won_invest_channel_server.domain.invest.dto.response.InvestEtfHoldingsResponse;
import com.woorifisa.won_invest_channel_server.domain.invest.exception.code.InvestErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestEtfQueryService {

    private final InvestChnAccountSummaryRepository accountSummaryRepository;
    private final InvestCoreEtfQueryClient investCoreEtfQueryClient;

    public InvestEtfHoldingsResponse getAccountEtfs(UUID userUuid, UUID accountUuid) {
        InvestChnAccountSummary account = accountSummaryRepository.findById(accountUuid)
                .orElseThrow(() -> new BusinessException(InvestErrorCode.ACCOUNT_NOT_FOUND));

        validateOwnership(userUuid, account);
        validateAccountStatus(account);

        return investCoreEtfQueryClient.fetchCoreEtfHoldings(userUuid, accountUuid);
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
}
