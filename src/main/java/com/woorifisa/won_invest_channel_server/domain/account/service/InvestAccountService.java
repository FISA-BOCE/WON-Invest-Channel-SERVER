package com.woorifisa.won_invest_channel_server.domain.account.service;

import com.woorifisa.won_invest_channel_server.domain.account.dto.request.CreateInvestAccountRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.request.LinkAccountRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.response.CreateInvestAccountResponse;
import com.woorifisa.won_invest_channel_server.domain.account.dto.response.LinkAccountResponse;
import com.woorifisa.won_invest_channel_server.domain.account.exception.code.InvestAccountErrorCode;
import com.woorifisa.won_invest_channel_server.domain.account.external.CommonMappingApi;
import com.woorifisa.won_invest_channel_server.domain.account.external.InvestCoreAccountApi;
import com.woorifisa.won_invest_channel_server.domain.account.external.dto.LinkInvestMappingRequest;
import com.woorifisa.won_invest_channel_server.domain.account.external.dto.MappingStatusResponse;
import com.woorifisa.won_invest_channel_server.domain.account.model.AccountStatus;
import com.woorifisa.won_invest_channel_server.domain.account.model.InvestChnAccountSummary;
import com.woorifisa.won_invest_channel_server.domain.account.repository.InvestChnAccountSummaryRepository;
import com.woorifisa.won_invest_channel_server.global.exception.code.CommonErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.util.JwtUtil;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestAccountService {

    private final CommonMappingApi commonMappingApi;
    private final InvestCoreAccountApi investCoreAccountApi;
    private final InvestChnAccountSummaryRepository accountSummaryRepository;
    private final JwtUtil jwtUtil;

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

    public CreateInvestAccountResponse openNewInvestAccount(
            CreateInvestAccountRequest request,
            String authorizationHeader
    ) {
        validatePasswordMatch(request.accountPassword(), request.accountPasswordConfirm());
        validateRequiredTerms(request.agreedTerms());
        jwtUtil.extractUserUuid(authorizationHeader);

        InvestCoreAccountApi.CoreApiResponse coreResponse;
        try {
            coreResponse = investCoreAccountApi.openNewInvestAccount(request);
        } catch (FeignException e) {
            throw mapFeignException(e);
        }

        if (coreResponse == null || coreResponse.data() == null) {
            throw new BusinessException(CommonErrorCode.BAD_GATEWAY);
        }

        return toChannelResponse(coreResponse.data());
    }

    private void validatePasswordMatch(String password, String passwordConfirm) {
        if (!java.util.Objects.equals(password, passwordConfirm)) {
            throw new BusinessException(InvestAccountErrorCode.PASSWORD_MISMATCH);
        }
    }

    private void validateRequiredTerms(List<String> agreedTerms) {
        if (agreedTerms == null || !agreedTerms.contains("INVEST_BASIC")) {
            throw new BusinessException(InvestAccountErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }
    }

    private BusinessException mapFeignException(FeignException e) {
        int status = e.status();
        if (status == 400) return new BusinessException(InvestAccountErrorCode.INVALID_INPUT);
        if (status == 401) return new BusinessException(CommonErrorCode.UNAUTHORIZED);
        if (status == 403) return new BusinessException(CommonErrorCode.FORBIDDEN);
        if (status >= 400 && status < 500) return new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        return new BusinessException(CommonErrorCode.BAD_GATEWAY);
    }

    private CreateInvestAccountResponse toChannelResponse(InvestCoreAccountApi.CoreAccountData coreData) {
        return new CreateInvestAccountResponse(
                coreData.investAccountUuid(),
                coreData.accountNoDisplay(),
                coreData.accountStatus(),
                coreData.investConnectedStatus(),
                coreData.openedAt()
        );
    }
}
import com.woorifisa.won_invest_channel_server.domain.account.dto.request.LinkAccountRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.response.LinkAccountResponse;
import com.woorifisa.won_invest_channel_server.domain.account.exception.code.InvestAccountErrorCode;
import com.woorifisa.won_invest_channel_server.domain.account.external.CommonMappingApi;
import com.woorifisa.won_invest_channel_server.domain.account.external.dto.LinkInvestMappingRequest;
import com.woorifisa.won_invest_channel_server.domain.account.external.dto.MappingStatusResponse;
import com.woorifisa.won_invest_channel_server.domain.account.model.AccountStatus;
import com.woorifisa.won_invest_channel_server.domain.account.model.InvestChnAccountSummary;
import com.woorifisa.won_invest_channel_server.domain.account.repository.InvestChnAccountSummaryRepository;