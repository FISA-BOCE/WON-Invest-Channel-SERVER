package com.woorifisa.won_invest_channel_server.domain.account.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorifisa.won_invest_channel_server.domain.account.dto.request.CreateInvestAccountChannelRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.request.CreateInvestAccountRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.request.LinkAccountRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.response.CreateInvestAccountResponse;
import com.woorifisa.won_invest_channel_server.domain.account.dto.response.LinkAccountResponse;
import com.woorifisa.won_invest_channel_server.domain.account.exception.code.InvestAccountErrorCode;
import com.woorifisa.won_invest_channel_server.domain.account.external.CommonMappingApi;
import com.woorifisa.won_invest_channel_server.domain.account.external.InvestCoreAccountApi;
import com.woorifisa.won_invest_channel_server.domain.account.external.code.AccountExternalErrorCode;
import com.woorifisa.won_invest_channel_server.domain.account.external.dto.InvestAccountCoreResponse;
import com.woorifisa.won_invest_channel_server.domain.account.external.dto.LinkInvestMappingRequest;
import com.woorifisa.won_invest_channel_server.domain.account.external.dto.MappingStatusResponse;
import com.woorifisa.won_invest_channel_server.domain.account.model.AccountStatus;
import com.woorifisa.won_invest_channel_server.domain.account.model.InvestChnAccountSummary;
import com.woorifisa.won_invest_channel_server.domain.account.repository.InvestChnAccountSummaryRepository;
import com.woorifisa.won_invest_channel_server.domain.auth.exception.code.AuthErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.code.CommonErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.response.ErrorResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestAccountService {

    private final CommonMappingApi commonMappingApi;
    private final InvestCoreAccountApi investCoreAccountApi;
    private final InvestChnAccountSummaryRepository accountSummaryRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public LinkAccountResponse linkAccount(UUID userUuid, LinkAccountRequest request) {
        ApiResponse<MappingStatusResponse> mappingStatusResponse;
        try {
            mappingStatusResponse = commonMappingApi.getMappingStatus(userUuid);
        } catch (FeignException.NotFound e) {
            if (isCommonUserMappingNotFound(e)) {
                throw new BusinessException(InvestAccountErrorCode.COMMON_USER_MAPPING_NOT_FOUND, e);
            }
            throw new BusinessException(CommonErrorCode.BAD_GATEWAY, e);
        } catch (FeignException e) {
            throw new BusinessException(CommonErrorCode.BAD_GATEWAY, e);
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
        } catch (FeignException.NotFound e) {
            if (isCommonUserMappingNotFound(e)) {
                throw new BusinessException(InvestAccountErrorCode.COMMON_USER_MAPPING_NOT_FOUND, e);
            }
            throw new BusinessException(CommonErrorCode.BAD_GATEWAY, e);
        } catch (FeignException e) {
            throw new BusinessException(CommonErrorCode.BAD_GATEWAY, e);
        }

        return new LinkAccountResponse(
                accountSummary.getInvestAccountUuid(),
                accountSummary.getAccountNoDisplay(),
                accountSummary.getAccountStatus().name(),
                true,
                LocalDateTime.now()
        );
    }

    @Transactional
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
            log.warn("Core server bad request [status={}]", e.status(), e);
            ExternalErrorCodeResolution errorCodeResolution = resolveExternalErrorCode(e);
            if (errorCodeResolution.is(AccountExternalErrorCode.CORE_ACCOUNT_ALREADY_CONNECTED)) {
                throw new BusinessException(InvestAccountErrorCode.ACCOUNT_ALREADY_CONNECTED, e);
            }
            if (errorCodeResolution.is(AccountExternalErrorCode.CORE_INVALID_INPUT)) {
                throw new BusinessException(InvestAccountErrorCode.INVALID_INPUT, e);
            }
            throw new BusinessException(CommonErrorCode.BAD_GATEWAY, e);
        } catch (FeignException.Unauthorized e) {
            log.warn("Core server unauthorized [status={}]", e.status(), e);
            throw new BusinessException(AuthErrorCode.AUTHENTICATION_REQUIRED, e);
        } catch (FeignException.Forbidden e) {
            log.warn("Core server forbidden [status={}]", e.status(), e);
            throw new BusinessException(AuthErrorCode.FORBIDDEN, e);
        } catch (FeignException e) {
            log.warn("Core server Feign error [status={}]", e.status(), e);
            if (isCoreAccountPersistenceFailed(e)) {
                throw new BusinessException(InvestAccountErrorCode.CORE_ACCOUNT_PERSISTENCE_FAILED, e);
            }
            throw new BusinessException(CommonErrorCode.BAD_GATEWAY, e);
        }

        if (coreResponse == null || coreResponse.data() == null) {
            throw new BusinessException(CommonErrorCode.BAD_GATEWAY);
        }

        InvestAccountCoreResponse coreData = coreResponse.data();

        accountSummaryRepository.save(InvestChnAccountSummary.builder()
                .investAccountUuid(coreData.investAccountUuid())
                .investUserUuid(coreData.investUserUuid())
                .userUuid(userUuid)
                .accountNoDisplay(coreData.accountNoDisplay())
                .accountStatus(AccountStatus.valueOf(coreData.accountStatus()))
                .build());

        try {
            commonMappingApi.linkInvestMapping(
                    userUuid,
                    new LinkInvestMappingRequest(coreData.investUserUuid())
            );
        } catch (FeignException.NotFound e) {
            log.warn("Common server linkInvestMapping not found [status={}]", e.status(), e);
            if (isCommonUserMappingNotFound(e)) {
                throw new BusinessException(InvestAccountErrorCode.COMMON_USER_MAPPING_NOT_FOUND, e);
            }
            throw new BusinessException(CommonErrorCode.BAD_GATEWAY, e);
        } catch (FeignException e) {
            log.warn("Common server linkInvestMapping Feign error [status={}]", e.status(), e);
            throw new BusinessException(CommonErrorCode.BAD_GATEWAY, e);
        }

        return toChannelResponse(coreData);
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

    private boolean isCommonUserMappingNotFound(FeignException e) {
        return hasExternalErrorCode(e, AccountExternalErrorCode.COMMON_USER_MAPPING_NOT_FOUND);
    }

    private boolean isCoreAccountPersistenceFailed(FeignException e) {
        return hasExternalErrorCode(e, AccountExternalErrorCode.CORE_ACCOUNT_PERSISTENCE_FAILED);
    }

    private boolean hasExternalErrorCode(FeignException e, AccountExternalErrorCode errorCode) {
        return resolveExternalErrorCode(e).is(errorCode);
    }

    private ExternalErrorCodeResolution resolveExternalErrorCode(FeignException e) {
        String body = e.contentUTF8();
        if (body == null || body.isBlank()) {
            return ExternalErrorCodeResolution.unreadable();
        }

        try {
            ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
            Optional<AccountExternalErrorCode> errorCode =
                    AccountExternalErrorCode.fromCode(errorResponse.code());
            return errorCode
                    .map(ExternalErrorCodeResolution::matched)
                    .orElseGet(() -> ExternalErrorCodeResolution.unknown(errorResponse.code()));
        } catch (JsonProcessingException ex) {
            log.warn("Failed to parse external error response [status={}]", e.status(), ex);
            return ExternalErrorCodeResolution.unreadable();
        }
    }

    private record ExternalErrorCodeResolution(
            boolean readable,
            AccountExternalErrorCode errorCode,
            String rawCode
    ) {

        private static ExternalErrorCodeResolution matched(AccountExternalErrorCode errorCode) {
            return new ExternalErrorCodeResolution(true, errorCode, errorCode.getCode());
        }

        private static ExternalErrorCodeResolution unknown(String rawCode) {
            return new ExternalErrorCodeResolution(true, null, rawCode);
        }

        private static ExternalErrorCodeResolution unreadable() {
            return new ExternalErrorCodeResolution(false, null, null);
        }

        private boolean is(AccountExternalErrorCode expectedErrorCode) {
            return readable && errorCode == expectedErrorCode;
        }
    }
}
