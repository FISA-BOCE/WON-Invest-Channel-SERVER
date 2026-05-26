package com.woorifisa.won_invest_channel_server.domain.account.service;

import com.woorifisa.won_invest_channel_server.domain.account.dto.request.CreateInvestAccountRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.response.CreateInvestAccountResponse;
import com.woorifisa.won_invest_channel_server.domain.account.exception.InvestAccountErrorCode;
import com.woorifisa.won_invest_channel_server.domain.account.external.InvestCoreAccountApi;
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
import com.woorifisa.won_invest_channel_server.global.util.JwtUtil;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.response.SuccessStatus;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
    @Mock private JwtUtil jwtUtil;
    @Mock
    private CommonMappingApi commonMappingApi;

    @Mock
    private InvestChnAccountSummaryRepository accountSummaryRepository;

    @InjectMocks
    private InvestAccountService investAccountService;

    private static final String AUTH_HEADER = "Bearer test.jwt.token";
    private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ACCOUNT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final LocalDateTime OPENED_AT = LocalDateTime.of(2026, 5, 25, 10, 0, 0);

    private CreateInvestAccountRequest validRequest() {
        return new CreateInvestAccountRequest(
                "010-1234-5678",
                "홍길동",
                "pass1234!",
                "pass1234!",
                "hong@example.com",
                List.of("INVEST_BASIC", "INVEST_AUTO")
        );
    }

    private InvestCoreAccountApi.CoreApiResponse coreSuccessResponse() {
        InvestCoreAccountApi.CoreAccountData data = new InvestCoreAccountApi.CoreAccountData(
                ACCOUNT_UUID,
                "123-***-***456",
                "ACTIVE",
                "CONNECTED",
                OPENED_AT
        );
        return new InvestCoreAccountApi.CoreApiResponse(201, "증권계좌 개설이 완료되었습니다.", data);
    }

    @Test
    @DisplayName("정상 연결 성공")
    void linkAccount_success() {
    @DisplayName("정상 요청 → Core 응답 매핑 성공")
    void openNewInvestAccount_success() {
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
        CreateInvestAccountRequest request = validRequest();
        given(jwtUtil.extractUserUuid(AUTH_HEADER)).willReturn(USER_UUID);
        given(investCoreAccountApi.openNewInvestAccount(any())).willReturn(coreSuccessResponse());

        // when
        CreateInvestAccountResponse response = investAccountService.openNewInvestAccount(request, AUTH_HEADER);
        LinkAccountResponse response = investAccountService.linkAccount(userUuid, request);

        // then
        assertThat(response.investAccountUuid()).isEqualTo(ACCOUNT_UUID);
        assertThat(response.accountNoDisplay()).isEqualTo("123-***-***456");
        assertThat(response.accountStatus()).isEqualTo("ACTIVE");
        assertThat(response.investConnectedStatus()).isEqualTo("CONNECTED");
        assertThat(response.openedAt()).isEqualTo(OPENED_AT);
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

        verify(jwtUtil).extractUserUuid(AUTH_HEADER);
        verify(investCoreAccountApi).openNewInvestAccount(request);
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
    @DisplayName("getMappingStatus Feign 오류 시 BAD_GATEWAY 예외 발생")
    void linkAccount_getMappingStatus_feignException() {
        // given
        UUID userUuid = UUID.randomUUID();
        UUID investAccountUuid = UUID.randomUUID();
        LinkAccountRequest request = new LinkAccountRequest(investAccountUuid);

        FeignException getStatusException = mock(FeignException.class);
        given(commonMappingApi.getMappingStatus(userUuid)).willThrow(getStatusException);

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(CommonErrorCode.BAD_GATEWAY));
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
    @DisplayName("비밀번호 불일치 시 PASSWORD_MISMATCH 예외")
    void openNewInvestAccount_passwordMismatch() {
    @DisplayName("getMappingStatus 응답이 null인 경우 BAD_GATEWAY 예외 발생")
    void linkAccount_mappingResponseNull_badGateway() {
        // given
        CreateInvestAccountRequest request = new CreateInvestAccountRequest(
                "010-1234-5678",
                "홍길동",
                "pass1234!",
                "wrong1234!",
                "hong@example.com",
                List.of("INVEST_BASIC")
        );
        UUID userUuid = UUID.randomUUID();
        LinkAccountRequest request = new LinkAccountRequest(UUID.randomUUID());

        // when / then
        assertThatThrownBy(() -> investAccountService.openNewInvestAccount(request, AUTH_HEADER))
        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.PASSWORD_MISMATCH));

        verify(jwtUtil, never()).extractUserUuid(any());
        verify(investCoreAccountApi, never()).openNewInvestAccount(any());
                        .isEqualTo(CommonErrorCode.BAD_GATEWAY));
    }

    @Test
    @DisplayName("필수 약관 미동의 시 REQUIRED_TERMS_NOT_AGREED 예외")
    void openNewInvestAccount_requiredTermsNotAgreed() {
    @DisplayName("getMappingStatus 응답의 data가 null인 경우 BAD_GATEWAY 예외 발생")
    void linkAccount_mappingResponseDataNull_badGateway() {
        // given
        CreateInvestAccountRequest request = new CreateInvestAccountRequest(
                "010-1234-5678",
                "홍길동",
                "pass1234!",
                "pass1234!",
                "hong@example.com",
                List.of("INVEST_AUTO")
        );
        UUID userUuid = UUID.randomUUID();
        LinkAccountRequest request = new LinkAccountRequest(UUID.randomUUID());

        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, (MappingStatusResponse) null);
        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
        // when / then
        assertThatThrownBy(() -> investAccountService.openNewInvestAccount(request, AUTH_HEADER))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(CommonErrorCode.BAD_GATEWAY));
                        .isEqualTo(InvestAccountErrorCode.REQUIRED_TERMS_NOT_AGREED));

        verify(jwtUtil, never()).extractUserUuid(any());
        verify(investCoreAccountApi, never()).openNewInvestAccount(any());
    }

    @Test
    @DisplayName("Core 서버 5xx FeignException 발생 시 BAD_GATEWAY 예외")
    void openNewInvestAccount_feignException_badGateway() {
    @DisplayName("getMappingStatus 응답의 data.invest()가 null인 경우 BAD_GATEWAY 예외 발생")
    void linkAccount_mappingResponseInvestNull_badGateway() {
        // given
        UUID userUuid = UUID.randomUUID();
        LinkAccountRequest request = new LinkAccountRequest(UUID.randomUUID());
        CreateInvestAccountRequest request = validRequest();
        given(jwtUtil.extractUserUuid(AUTH_HEADER)).willReturn(USER_UUID);

        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, new MappingStatusResponse(null));
        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);
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
        given(investCoreAccountApi.openNewInvestAccount(any())).willThrow(feignException);

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
        // when / then
        assertThatThrownBy(() -> investAccountService.openNewInvestAccount(request, AUTH_HEADER))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(CommonErrorCode.BAD_GATEWAY));

        verify(jwtUtil).extractUserUuid(AUTH_HEADER);
    }
}
