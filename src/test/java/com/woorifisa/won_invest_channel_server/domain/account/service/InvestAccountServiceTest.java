package com.woorifisa.won_invest_channel_server.domain.account.service;

import com.woorifisa.won_invest_channel_server.domain.account.dto.request.CreateInvestAccountRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.response.CreateInvestAccountResponse;
import com.woorifisa.won_invest_channel_server.domain.account.exception.InvestAccountErrorCode;
import com.woorifisa.won_invest_channel_server.domain.account.external.InvestCoreAccountApi;
import com.woorifisa.won_invest_channel_server.global.exception.code.CommonErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_channel_server.global.util.JwtUtil;
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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class InvestAccountServiceTest {

    @Mock private InvestCoreAccountApi investCoreAccountApi;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private InvestAccountService investAccountService;

    private static final String AUTH_HEADER = "Bearer test.jwt.token";
    private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ACCOUNT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

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
                Instant.parse("2026-05-25T10:00:00Z")
        );
        return new InvestCoreAccountApi.CoreApiResponse(201, "증권계좌 개설이 완료되었습니다.", data);
    }

    @Test
    @DisplayName("정상 요청 → Core 응답 매핑 성공")
    void openNewInvestAccount_success() {
        // given
        CreateInvestAccountRequest request = validRequest();
        given(jwtUtil.extractUserUuid(AUTH_HEADER)).willReturn(USER_UUID);
        given(investCoreAccountApi.openNewInvestAccount(any())).willReturn(coreSuccessResponse());

        // when
        CreateInvestAccountResponse response = investAccountService.openNewInvestAccount(request, AUTH_HEADER);

        // then
        assertThat(response.investAccountUuid()).isEqualTo(ACCOUNT_UUID);
        assertThat(response.accountNoDisplay()).isEqualTo("123-***-***456");
        assertThat(response.accountStatus()).isEqualTo("ACTIVE");
        assertThat(response.investConnectedStatus()).isEqualTo("CONNECTED");
        assertThat(response.openedAt()).isEqualTo(Instant.parse("2026-05-25T10:00:00Z"));

        verify(investCoreAccountApi).openNewInvestAccount(request);
    }

    @Test
    @DisplayName("비밀번호 불일치 시 PASSWORD_MISMATCH 예외")
    void openNewInvestAccount_passwordMismatch() {
        // given
        CreateInvestAccountRequest request = new CreateInvestAccountRequest(
                "010-1234-5678",
                "홍길동",
                "pass1234!",
                "wrong1234!",
                "hong@example.com",
                List.of("INVEST_BASIC")
        );

        // when / then
        assertThatThrownBy(() -> investAccountService.openNewInvestAccount(request, AUTH_HEADER))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.PASSWORD_MISMATCH));

        verify(jwtUtil, never()).extractUserUuid(any());
        verify(investCoreAccountApi, never()).openNewInvestAccount(any());
    }

    @Test
    @DisplayName("필수 약관 미동의 시 REQUIRED_TERMS_NOT_AGREED 예외")
    void openNewInvestAccount_requiredTermsNotAgreed() {
        // given
        CreateInvestAccountRequest request = new CreateInvestAccountRequest(
                "010-1234-5678",
                "홍길동",
                "pass1234!",
                "pass1234!",
                "hong@example.com",
                List.of("INVEST_AUTO")
        );

        // when / then
        assertThatThrownBy(() -> investAccountService.openNewInvestAccount(request, AUTH_HEADER))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.REQUIRED_TERMS_NOT_AGREED));

        verify(jwtUtil, never()).extractUserUuid(any());
        verify(investCoreAccountApi, never()).openNewInvestAccount(any());
    }

    @Test
    @DisplayName("Core 서버 FeignException 발생 시 BAD_GATEWAY 예외")
    void openNewInvestAccount_feignException_badGateway() {
        // given
        CreateInvestAccountRequest request = validRequest();
        given(jwtUtil.extractUserUuid(AUTH_HEADER)).willReturn(USER_UUID);

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

        // when / then
        assertThatThrownBy(() -> investAccountService.openNewInvestAccount(request, AUTH_HEADER))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(CommonErrorCode.BAD_GATEWAY));
    }
}