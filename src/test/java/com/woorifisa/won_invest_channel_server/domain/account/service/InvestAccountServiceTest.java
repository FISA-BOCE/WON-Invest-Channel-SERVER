package com.woorifisa.won_invest_channel_server.domain.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorifisa.won_invest_channel_server.domain.account.dto.request.CreateInvestAccountChannelRequest;
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
import com.woorifisa.won_invest_channel_server.global.response.SuccessStatus;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class InvestAccountServiceTest {

    @Mock private InvestCoreAccountApi investCoreAccountApi;
    @Mock private CommonMappingApi commonMappingApi;
    @Mock private InvestChnAccountSummaryRepository accountSummaryRepository;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private InvestAccountService investAccountService;

    private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ACCOUNT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID INVEST_USER_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final LocalDateTime OPENED_AT = LocalDateTime.of(2026, 5, 25, 10, 0, 0);

    private CreateInvestAccountChannelRequest validRequest() {
        return new CreateInvestAccountChannelRequest(
                "010-1234-5678",
                "홍길동",
                "pass1234!",
                "pass1234!",
                "hong@example.com",
                List.of("INVEST_BASIC", "INVEST_AUTO")
        );
    }

    private ApiResponse<InvestAccountCoreResponse> coreSuccessResponse() {
        InvestAccountCoreResponse data = new InvestAccountCoreResponse(
                ACCOUNT_UUID,
                INVEST_USER_UUID,
                "123-***-***456",
                "ACTIVE",
                "CONNECTED",
                OPENED_AT
        );
        return ApiResponse.of(SuccessStatus.OK, data);
    }

    // -------------------------------------------------------------------------
    // createNewInvestAccount
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("정상 요청 → Core 응답 매핑 성공")
    void createNewInvestAccount_success() {
        // given
        CreateInvestAccountChannelRequest request = validRequest();
        given(investCoreAccountApi.createNewInvestAccount(eq(USER_UUID), any())).willReturn(coreSuccessResponse());

        // when
        CreateInvestAccountResponse response = investAccountService.createNewInvestAccount(request, USER_UUID);

        // then
        assertThat(response.investAccountUuid()).isEqualTo(ACCOUNT_UUID);
        assertThat(response.accountNoDisplay()).isEqualTo("123-***-***456");
        assertThat(response.accountStatus()).isEqualTo("ACTIVE");
        assertThat(response.investConnectedStatus()).isEqualTo("CONNECTED");
        assertThat(response.openedAt()).isEqualTo(OPENED_AT);
        verify(accountSummaryRepository).save(any(InvestChnAccountSummary.class));
        verify(commonMappingApi).linkInvestMapping(eq(USER_UUID), eq(new LinkInvestMappingRequest(INVEST_USER_UUID)));
    }

    @Test
    @DisplayName("비밀번호 불일치 시 PASSWORD_MISMATCH 예외")
    void createNewInvestAccount_passwordMismatch() {
        // given
        CreateInvestAccountChannelRequest request = new CreateInvestAccountChannelRequest(
                "010-1234-5678",
                "홍길동",
                "pass1234!",
                "wrong1234!",
                "hong@example.com",
                List.of("INVEST_BASIC")
        );

        // when / then
        assertThatThrownBy(() -> investAccountService.createNewInvestAccount(request, USER_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.PASSWORD_MISMATCH));

        verify(investCoreAccountApi, never()).createNewInvestAccount(any(), any());
    }

    @Test
    @DisplayName("필수 약관 미동의 시 REQUIRED_TERMS_NOT_AGREED 예외")
    void createNewInvestAccount_requiredTermsNotAgreed() {
        // given
        CreateInvestAccountChannelRequest request = new CreateInvestAccountChannelRequest(
                "010-1234-5678",
                "홍길동",
                "pass1234!",
                "pass1234!",
                "hong@example.com",
                List.of("INVEST_AUTO")
        );

        // when / then
        assertThatThrownBy(() -> investAccountService.createNewInvestAccount(request, USER_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.REQUIRED_TERMS_NOT_AGREED));

        verify(investCoreAccountApi, never()).createNewInvestAccount(any(), any());
    }

    @Test
    @DisplayName("Core 서버 400 INVEST_400_003 → ACCOUNT_ALREADY_CONNECTED 예외")
    void createNewInvestAccount_feignException_alreadyConnected() {
        // given
        CreateInvestAccountChannelRequest request = validRequest();

        Request dummyRequest = Request.create(
                Request.HttpMethod.POST,
                "http://core/internal/invest/accounts/new",
                Collections.emptyMap(),
                null,
                new RequestTemplate()
        );
        byte[] body = "{\"status\":400,\"code\":\"INVEST_400_003\",\"message\":\"이미 연결된 증권계좌가 존재합니다.\"}"
                .getBytes(StandardCharsets.UTF_8);
        FeignException.BadRequest badRequestException = new FeignException.BadRequest(
                "Already connected", dummyRequest, body, null
        );
        given(investCoreAccountApi.createNewInvestAccount(eq(USER_UUID), any())).willThrow(badRequestException);

        // when / then
        assertThatThrownBy(() -> investAccountService.createNewInvestAccount(request, USER_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.ACCOUNT_ALREADY_CONNECTED));
    }

    @Test
    @DisplayName("Core 서버 400 기타 에러 → INVALID_INPUT 예외")
    void createNewInvestAccount_feignException_badRequest_invalidInput() {
        // given
        CreateInvestAccountChannelRequest request = validRequest();

        Request dummyRequest = Request.create(
                Request.HttpMethod.POST,
                "http://core/internal/invest/accounts/new",
                Collections.emptyMap(),
                null,
                new RequestTemplate()
        );
        byte[] body = "{\"status\":400,\"code\":\"INVEST_400_001\",\"message\":\"입력값 형식이 올바르지 않습니다.\"}"
                .getBytes(StandardCharsets.UTF_8);
        FeignException.BadRequest badRequestException = new FeignException.BadRequest(
                "Invalid input", dummyRequest, body, null
        );
        given(investCoreAccountApi.createNewInvestAccount(eq(USER_UUID), any())).willThrow(badRequestException);

        // when / then
        assertThatThrownBy(() -> investAccountService.createNewInvestAccount(request, USER_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.INVALID_INPUT));
    }

    @Test
    @DisplayName("Core 서버 5xx FeignException 발생 시 BAD_GATEWAY 예외")
    void createNewInvestAccount_feignException_badGateway() {
        // given
        CreateInvestAccountChannelRequest request = validRequest();

        Request dummyRequest = Request.create(
                Request.HttpMethod.POST,
                "http://core/internal/invest/accounts/new",
                Collections.emptyMap(),
                null,
                new RequestTemplate()
        );
        FeignException feignException = new FeignException.ServiceUnavailable(
                "Core server unavailable", dummyRequest, null, null
        );
        given(investCoreAccountApi.createNewInvestAccount(eq(USER_UUID), any())).willThrow(feignException);

        // when / then
        assertThatThrownBy(() -> investAccountService.createNewInvestAccount(request, USER_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(CommonErrorCode.BAD_GATEWAY));
    }

    @Test
    @DisplayName("Core 서버 INVEST_500_001 발생 시 summary를 저장하지 않고 CORE_ACCOUNT_PERSISTENCE_FAILED 예외")
    void createNewInvestAccount_feignException_corePersistenceFailed() {
        // given
        CreateInvestAccountChannelRequest request = validRequest();

        Request dummyRequest = Request.create(
                Request.HttpMethod.POST,
                "http://core/internal/invest/accounts/new",
                Collections.emptyMap(),
                null,
                new RequestTemplate()
        );
        byte[] body = "{\"status\":500,\"code\":\"INVEST_500_001\",\"message\":\"증권계좌 원본 정보 저장에 실패했습니다.\"}"
                .getBytes(StandardCharsets.UTF_8);
        FeignException.InternalServerError feignException = new FeignException.InternalServerError(
                "Core account persistence failed", dummyRequest, body, null
        );
        given(investCoreAccountApi.createNewInvestAccount(eq(USER_UUID), any())).willThrow(feignException);

        // when / then
        assertThatThrownBy(() -> investAccountService.createNewInvestAccount(request, USER_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.CORE_ACCOUNT_PERSISTENCE_FAILED));

        verify(accountSummaryRepository, never()).save(any());
        verify(commonMappingApi, never()).linkInvestMapping(any(), any());
    }

    @Test
    @DisplayName("계좌 동기화 저장 후 linkInvestMapping Feign 오류 시 BAD_GATEWAY 예외")
    void createNewInvestAccount_linkInvestMapping_feignException() {
        // given
        CreateInvestAccountChannelRequest request = validRequest();
        given(investCoreAccountApi.createNewInvestAccount(eq(USER_UUID), any())).willReturn(coreSuccessResponse());
        FeignException feignException = mock(FeignException.class);
        willThrow(feignException).given(commonMappingApi).linkInvestMapping(eq(USER_UUID), any());

        // when / then
        assertThatThrownBy(() -> investAccountService.createNewInvestAccount(request, USER_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(CommonErrorCode.BAD_GATEWAY));
        verify(accountSummaryRepository).save(any(InvestChnAccountSummary.class));
    }

    @Test
    @DisplayName("계좌 동기화 저장 후 공통 사용자 매핑이 없으면 COMMON_USER_MAPPING_NOT_FOUND 예외")
    void createNewInvestAccount_linkInvestMapping_mappingNotFound() {
        CreateInvestAccountChannelRequest request = validRequest();
        given(investCoreAccountApi.createNewInvestAccount(eq(USER_UUID), any())).willReturn(coreSuccessResponse());

        Request dummyRequest = Request.create(
                Request.HttpMethod.PATCH,
                "http://common/internal/mappings/users/" + USER_UUID + "/invest",
                Collections.emptyMap(),
                null,
                new RequestTemplate()
        );
        byte[] body = "{\"status\":404,\"code\":\"MAP_404_001\",\"message\":\"고객 매핑 정보를 찾을 수 없습니다.\"}"
                .getBytes(StandardCharsets.UTF_8);
        FeignException.NotFound notFoundException = new FeignException.NotFound(
                "Mapping not found", dummyRequest, body, null
        );
        willThrow(notFoundException).given(commonMappingApi).linkInvestMapping(eq(USER_UUID), any());

        assertThatThrownBy(() -> investAccountService.createNewInvestAccount(request, USER_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.COMMON_USER_MAPPING_NOT_FOUND));
    }

    // -------------------------------------------------------------------------
    // linkAccount
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("정상 연결 성공")
    void linkAccount_success() {
        // given
        UUID userUuid = UUID.randomUUID();
        UUID investAccountUuid = UUID.randomUUID();
        UUID investUserUuid = UUID.randomUUID();

        LinkAccountRequest request = new LinkAccountRequest(investAccountUuid);

        MappingStatusResponse mappingStatus = new MappingStatusResponse(new MappingStatusResponse.InvestStatus(false));
        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, mappingStatus);

        InvestChnAccountSummary accountSummary = mock(InvestChnAccountSummary.class);
        given(accountSummary.getUserUuid()).willReturn(userUuid);
        given(accountSummary.getAccountStatus()).willReturn(AccountStatus.ACTIVE);
        given(accountSummary.getInvestAccountUuid()).willReturn(investAccountUuid);
        given(accountSummary.getInvestUserUuid()).willReturn(investUserUuid);
        given(accountSummary.getAccountNoDisplay()).willReturn("123-456-789");

        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);
        given(accountSummaryRepository.findById(investAccountUuid)).willReturn(Optional.of(accountSummary));

        // when
        LinkAccountResponse response = investAccountService.linkAccount(userUuid, request);

        // then
        assertThat(response.investAccountUuid()).isEqualTo(investAccountUuid);
        assertThat(response.accountNoDisplay()).isEqualTo("123-456-789");
        assertThat(response.investConnectedStatus()).isTrue();
        verify(commonMappingApi).linkInvestMapping(eq(userUuid), eq(new LinkInvestMappingRequest(investUserUuid)));
    }

    @Test
    @DisplayName("이미 연결된 경우 ALREADY_LINKED 예외 발생")
    void linkAccount_alreadyLinked() {
        // given
        UUID userUuid = UUID.randomUUID();
        UUID investAccountUuid = UUID.randomUUID();

        LinkAccountRequest request = new LinkAccountRequest(investAccountUuid);

        MappingStatusResponse mappingStatus = new MappingStatusResponse(new MappingStatusResponse.InvestStatus(true));
        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, mappingStatus);

        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.ALREADY_LINKED));
    }

    @Test
    @DisplayName("계좌 없는 경우 ACCOUNT_NOT_FOUND 예외 발생")
    void linkAccount_accountNotFound() {
        // given
        UUID userUuid = UUID.randomUUID();
        UUID investAccountUuid = UUID.randomUUID();

        LinkAccountRequest request = new LinkAccountRequest(investAccountUuid);

        MappingStatusResponse mappingStatus = new MappingStatusResponse(new MappingStatusResponse.InvestStatus(false));
        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, mappingStatus);

        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);
        given(accountSummaryRepository.findById(investAccountUuid)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.ACCOUNT_NOT_FOUND));
    }

    @Test
    @DisplayName("본인 계좌가 아닌 경우 NOT_ACCOUNT_OWNER 예외 발생")
    void linkAccount_notAccountOwner() {
        // given
        UUID userUuid = UUID.randomUUID();
        UUID investAccountUuid = UUID.randomUUID();
        UUID otherUserUuid = UUID.randomUUID();

        LinkAccountRequest request = new LinkAccountRequest(investAccountUuid);

        MappingStatusResponse mappingStatus = new MappingStatusResponse(new MappingStatusResponse.InvestStatus(false));
        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, mappingStatus);

        InvestChnAccountSummary accountSummary = mock(InvestChnAccountSummary.class);
        given(accountSummary.getUserUuid()).willReturn(otherUserUuid);

        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);
        given(accountSummaryRepository.findById(investAccountUuid)).willReturn(Optional.of(accountSummary));

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.NOT_ACCOUNT_OWNER));
    }

    @Test
    @DisplayName("계좌 상태 비정상인 경우 INVALID_ACCOUNT_STATUS 예외 발생")
    void linkAccount_invalidAccountStatus() {
        // given
        UUID userUuid = UUID.randomUUID();
        UUID investAccountUuid = UUID.randomUUID();

        LinkAccountRequest request = new LinkAccountRequest(investAccountUuid);

        MappingStatusResponse mappingStatus = new MappingStatusResponse(new MappingStatusResponse.InvestStatus(false));
        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, mappingStatus);

        InvestChnAccountSummary accountSummary = mock(InvestChnAccountSummary.class);
        given(accountSummary.getUserUuid()).willReturn(userUuid);
        given(accountSummary.getAccountStatus()).willReturn(AccountStatus.INACTIVE);

        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);
        given(accountSummaryRepository.findById(investAccountUuid)).willReturn(Optional.of(accountSummary));

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.INVALID_ACCOUNT_STATUS));
    }

    @Test
    @DisplayName("getMappingStatus Feign 오류 시 BAD_GATEWAY 예외 발생")
    void linkAccount_getMappingStatus_feignException() {
        // given
        UUID userUuid = UUID.randomUUID();
        LinkAccountRequest request = new LinkAccountRequest(UUID.randomUUID());

        FeignException getStatusException = mock(FeignException.class);
        given(commonMappingApi.getMappingStatus(userUuid)).willThrow(getStatusException);

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(CommonErrorCode.BAD_GATEWAY));
    }

    @Test
    @DisplayName("getMappingStatus에서 공통 사용자 매핑이 없으면 COMMON_USER_MAPPING_NOT_FOUND 예외 발생")
    void linkAccount_getMappingStatus_mappingNotFound() {
        UUID userUuid = UUID.randomUUID();
        LinkAccountRequest request = new LinkAccountRequest(UUID.randomUUID());

        Request dummyRequest = Request.create(
                Request.HttpMethod.GET,
                "http://common/internal/mappings/users/" + userUuid,
                Collections.emptyMap(),
                null,
                new RequestTemplate()
        );
        byte[] body = "{\"status\":404,\"code\":\"MAP_404_001\",\"message\":\"고객 매핑 정보를 찾을 수 없습니다.\"}"
                .getBytes(StandardCharsets.UTF_8);
        FeignException.NotFound notFoundException = new FeignException.NotFound(
                "Mapping not found", dummyRequest, body, null
        );
        given(commonMappingApi.getMappingStatus(userUuid)).willThrow(notFoundException);

        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.COMMON_USER_MAPPING_NOT_FOUND));
    }

    @Test
    @DisplayName("linkInvestMapping에서 공통 사용자 매핑이 없으면 COMMON_USER_MAPPING_NOT_FOUND 예외 발생")
    void linkAccount_linkInvestMapping_mappingNotFound() {
        UUID userUuid = UUID.randomUUID();
        UUID investAccountUuid = UUID.randomUUID();
        UUID investUserUuid = UUID.randomUUID();
        LinkAccountRequest request = new LinkAccountRequest(investAccountUuid);

        MappingStatusResponse mappingStatus = new MappingStatusResponse(new MappingStatusResponse.InvestStatus(false));
        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, mappingStatus);

        InvestChnAccountSummary accountSummary = mock(InvestChnAccountSummary.class);
        given(accountSummary.getUserUuid()).willReturn(userUuid);
        given(accountSummary.getAccountStatus()).willReturn(AccountStatus.ACTIVE);
        given(accountSummary.getInvestUserUuid()).willReturn(investUserUuid);

        Request dummyRequest = Request.create(
                Request.HttpMethod.PATCH,
                "http://common/internal/mappings/users/" + userUuid + "/invest",
                Collections.emptyMap(),
                null,
                new RequestTemplate()
        );
        byte[] body = "{\"status\":404,\"code\":\"MAP_404_001\",\"message\":\"고객 매핑 정보를 찾을 수 없습니다.\"}"
                .getBytes(StandardCharsets.UTF_8);
        FeignException.NotFound notFoundException = new FeignException.NotFound(
                "Mapping not found", dummyRequest, body, null
        );

        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);
        given(accountSummaryRepository.findById(investAccountUuid)).willReturn(Optional.of(accountSummary));
        willThrow(notFoundException).given(commonMappingApi).linkInvestMapping(eq(userUuid), any());

        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.COMMON_USER_MAPPING_NOT_FOUND))
                .hasCause(notFoundException);
    }

    @Test
    @DisplayName("linkInvestMapping Feign 오류 시 BAD_GATEWAY 예외 발생")
    void linkAccount_linkInvestMapping_feignException() {
        // given
        UUID userUuid = UUID.randomUUID();
        UUID investAccountUuid = UUID.randomUUID();
        UUID investUserUuid = UUID.randomUUID();
        LinkAccountRequest request = new LinkAccountRequest(investAccountUuid);

        MappingStatusResponse mappingStatus = new MappingStatusResponse(new MappingStatusResponse.InvestStatus(false));
        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, mappingStatus);

        InvestChnAccountSummary accountSummary = mock(InvestChnAccountSummary.class);
        given(accountSummary.getUserUuid()).willReturn(userUuid);
        given(accountSummary.getAccountStatus()).willReturn(AccountStatus.ACTIVE);
        given(accountSummary.getInvestUserUuid()).willReturn(investUserUuid);

        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);
        given(accountSummaryRepository.findById(investAccountUuid)).willReturn(Optional.of(accountSummary));
        FeignException linkMappingException = mock(FeignException.class);
        willThrow(linkMappingException).given(commonMappingApi).linkInvestMapping(eq(userUuid), any());

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(CommonErrorCode.BAD_GATEWAY));
    }

    @Test
    @DisplayName("getMappingStatus 응답이 null인 경우 BAD_GATEWAY 예외 발생")
    void linkAccount_mappingResponseNull_badGateway() {
        // given
        UUID userUuid = UUID.randomUUID();
        LinkAccountRequest request = new LinkAccountRequest(UUID.randomUUID());

        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(CommonErrorCode.BAD_GATEWAY));
    }

    @Test
    @DisplayName("getMappingStatus 응답의 data가 null인 경우 BAD_GATEWAY 예외 발생")
    void linkAccount_mappingResponseDataNull_badGateway() {
        // given
        UUID userUuid = UUID.randomUUID();
        LinkAccountRequest request = new LinkAccountRequest(UUID.randomUUID());

        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, (MappingStatusResponse) null);
        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(CommonErrorCode.BAD_GATEWAY));
    }

    @Test
    @DisplayName("getMappingStatus 응답의 data.invest()가 null인 경우 BAD_GATEWAY 예외 발생")
    void linkAccount_mappingResponseInvestNull_badGateway() {
        // given
        UUID userUuid = UUID.randomUUID();
        LinkAccountRequest request = new LinkAccountRequest(UUID.randomUUID());

        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, new MappingStatusResponse(null));
        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(CommonErrorCode.BAD_GATEWAY));
    }
}
