package com.woorifisa.won_invest_channel_server.domain.account.service;

import com.woorifisa.won_invest_channel_server.domain.account.dto.request.CreateInvestAccountChannelRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.request.CreateInvestAccountRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.request.LinkAccountRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.response.CreateInvestAccountResponse;
import com.woorifisa.won_invest_channel_server.domain.account.dto.response.LinkAccountResponse;
import com.woorifisa.won_invest_channel_server.domain.account.exception.code.InvestAccountErrorCode;
import com.woorifisa.won_invest_channel_server.domain.account.external.CommonMappingApi;
import com.woorifisa.won_invest_channel_server.domain.account.external.InvestCoreAccountApi;
import com.woorifisa.won_invest_channel_server.domain.account.external.dto.InvestAccountCoreResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestAccountService {

    private final CommonMappingApi commonMappingApi;
    private final InvestCoreAccountApi investCoreAccountApi;
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

    public CreateInvestAccountResponse createNewInvestAccount(
            CreateInvestAccountChannelRequest request,
            UUID userUuid
    ) {
        validatePasswordMatch(request.accountPassword(), request.accountPasswordConfirm());
        validateRequiredTerms(request.agreedTerms());

        CreateInvestAccountRequest coreRequest = new CreateInvestAccountRequest(
                request.phoneNumber(),
                request.customerName(),
                request.accountPassword(),
                request.email(),
                request.agreedTerms()
        );

        ApiResponse<InvestAccountCoreResponse> coreResponse;
        try {
            coreResponse = investCoreAccountApi.createNewInvestAccount(userUuid, coreRequest);
        } catch (FeignException.BadRequest e) {
            log.warn("Core server bad request [status={}]", e.status());
            String body = e.contentUTF8();
            if (body != null && body.contains("INVEST_400_003")) {
                throw new BusinessException(InvestAccountErrorCode.ACCOUNT_ALREADY_CONNECTED);
            }
            throw new BusinessException(InvestAccountErrorCode.INVALID_INPUT);
        } catch (FeignException.Unauthorized e) {
            log.warn("Core server unauthorized [status={}]", e.status());
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        } catch (FeignException.Forbidden e) {
            log.warn("Core server forbidden [status={}]", e.status());
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        } catch (FeignException e) {
            log.warn("Core server Feign error [status={}]", e.status());
            throw new BusinessException(CommonErrorCode.BAD_GATEWAY);
        }

        if (coreResponse == null || coreResponse.data() == null) {
            throw new BusinessException(CommonErrorCode.BAD_GATEWAY);
        }

        return toChannelResponse(coreResponse.data());
    }

    private void validatePasswordMatch(String password, String passwordConfirm) {
        if (!Objects.equals(password, passwordConfirm)) {
            throw new BusinessException(InvestAccountErrorCode.PASSWORD_MISMATCH);
        }
    }

    private void validateRequiredTerms(List<String> agreedTerms) {
        if (agreedTerms == null || !agreedTerms.contains("INVEST_BASIC")) {
            throw new BusinessException(InvestAccountErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }
    }

    private CreateInvestAccountResponse toChannelResponse(InvestAccountCoreResponse coreData) {
        return new CreateInvestAccountResponse(
                coreData.investAccountUuid(),
                coreData.accountNoDisplay(),
                coreData.accountStatus(),
                coreData.investConnectedStatus(),
                coreData.openedAt()
        );
    }
}
