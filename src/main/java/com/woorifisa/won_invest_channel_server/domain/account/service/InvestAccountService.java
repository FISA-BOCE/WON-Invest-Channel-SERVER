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
import java.util.UUID;

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

        UUID userUuid = jwtUtil.extractUserUuid(authorizationHeader);

        InvestCoreAccountApi.CoreApiResponse coreResponse;
        try {
            coreResponse = investCoreAccountApi.openNewInvestAccount(request);
        } catch (FeignException e) {
            throw new BusinessException(CommonErrorCode.BAD_GATEWAY);
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