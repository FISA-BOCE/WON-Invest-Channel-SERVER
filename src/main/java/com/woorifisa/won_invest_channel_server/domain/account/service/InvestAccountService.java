package com.woorifisa.won_invest_channel_server.domain.account.service;

import com.woorifisa.won_invest_channel_server.domain.account.dto.request.CreateInvestAccountRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.response.CreateInvestAccountResponse;
import com.woorifisa.won_invest_channel_server.domain.account.exception.InvestAccountErrorCode;
import com.woorifisa.won_invest_channel_server.domain.account.external.InvestCoreAccountApi;
import com.woorifisa.won_invest_channel_server.global.exception.code.CommonErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_channel_server.global.util.JwtUtil;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvestAccountService {

    private final InvestCoreAccountApi investCoreAccountApi;
    private final JwtUtil jwtUtil;

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
        if (!password.equals(passwordConfirm)) {
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