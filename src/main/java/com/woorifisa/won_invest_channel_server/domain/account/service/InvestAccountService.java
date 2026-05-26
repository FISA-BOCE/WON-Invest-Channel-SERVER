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
import com.woorifisa.won_invest_channel_server.global.exception.code.CommonErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestAccountService {

    private final CommonMappingApi commonMappingApi;
    private final InvestChnAccountSummaryRepository accountSummaryRepository;

    @Transactional
    public LinkAccountResponse linkAccount(UUID userUuid, LinkAccountRequest request) {
        ApiResponse<MappingStatusResponse> mappingStatusResponse;
        try {
            mappingStatusResponse = commonMappingApi.getMappingStatus(userUuid);
        } catch (FeignException e) {
            throw new BusinessException(CommonErrorCode.BAD_GATEWAY);
        }
        if (mappingStatusResponse == null || mappingStatusResponse.data() == null
                || mappingStatusResponse.data().invest() == null) {
            throw new BusinessException(CommonErrorCode.BAD_GATEWAY);
        }
        if (mappingStatusResponse.data().invest().isConnected()) {
            throw new BusinessException(InvestAccountErrorCode.ALREADY_LINKED);
        }

        InvestChnAccountSummary accountSummary = accountSummaryRepository
                .findById(request.investAccountUuid())
                .orElseThrow(() -> new BusinessException(InvestAccountErrorCode.ACCOUNT_NOT_FOUND));

        if (!accountSummary.getUserUuid().equals(userUuid)) {
            throw new BusinessException(InvestAccountErrorCode.NOT_ACCOUNT_OWNER);
        }

        if (accountSummary.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(InvestAccountErrorCode.INVALID_ACCOUNT_STATUS);
        }

        try {
            commonMappingApi.linkInvestMapping(
                    userUuid,
                    new LinkInvestMappingRequest(accountSummary.getInvestUserUuid())
            );
        } catch (FeignException e) {
            throw new BusinessException(CommonErrorCode.BAD_GATEWAY);
        }

        return new LinkAccountResponse(
                accountSummary.getInvestAccountUuid(),
                accountSummary.getAccountNoDisplay(),
                accountSummary.getAccountStatus().name(),
                true,
                LocalDateTime.now()
        );
    }
}
