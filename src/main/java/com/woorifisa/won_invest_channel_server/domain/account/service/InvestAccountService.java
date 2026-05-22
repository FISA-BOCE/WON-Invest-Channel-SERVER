package com.woorifisa.won_invest_channel_server.domain.account.service;

import com.woorifisa.won_invest_channel_server.domain.account.dto.request.LinkAccountRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.response.LinkAccountResponse;
import com.woorifisa.won_invest_channel_server.domain.account.exception.code.InvestAccountErrorCode;
import com.woorifisa.won_invest_channel_server.domain.account.external.CommonMappingApi;
import com.woorifisa.won_invest_channel_server.domain.account.external.dto.LinkInvestMappingRequest;
import com.woorifisa.won_invest_channel_server.domain.account.external.dto.MappingStatusResponse;
import com.woorifisa.won_invest_channel_server.domain.account.model.AccountStatus;
import com.woorifisa.won_invest_channel_server.domain.account.model.InvestChnAccountSummary;
import com.woorifisa.won_invest_channel_server.domain.account.repository.InvestChnAccountSummaryRepository;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestAccountService {

    private final CommonMappingApi commonMappingApi;
    private final InvestChnAccountSummaryRepository accountSummaryRepository;

    @Transactional
    public LinkAccountResponse linkAccount(String userUuid, LinkAccountRequest request) {
        MappingStatusResponse mappingStatus = commonMappingApi.getMappingStatus(userUuid);
        if (mappingStatus.invest().isConnected()) {
            throw new BusinessException(InvestAccountErrorCode.ALREADY_LINKED);
        }

        InvestChnAccountSummary accountSummary = accountSummaryRepository
                .findByInvestAccountUuid(request.investAccountUuid())
                .orElseThrow(() -> new BusinessException(InvestAccountErrorCode.ACCOUNT_NOT_FOUND));

        if (!accountSummary.getUserUuid().equals(userUuid)) {
            throw new BusinessException(InvestAccountErrorCode.NOT_ACCOUNT_OWNER);
        }

        if (accountSummary.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(InvestAccountErrorCode.INVALID_ACCOUNT_STATUS);
        }

        commonMappingApi.linkInvestMapping(userUuid,
                new LinkInvestMappingRequest(request.investAccountUuid()));

        return new LinkAccountResponse(
                accountSummary.getInvestAccountUuid(),
                accountSummary.getUserUuid()
        );
    }
}
