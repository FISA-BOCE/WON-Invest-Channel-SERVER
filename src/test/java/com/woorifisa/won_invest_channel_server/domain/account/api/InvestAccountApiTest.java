package com.woorifisa.won_invest_channel_server.domain.account.api;

import com.woorifisa.won_invest_channel_server.domain.auth.exception.code.AuthErrorCode;
import com.woorifisa.won_invest_channel_server.domain.account.dto.request.LinkAccountRequest;
import com.woorifisa.won_invest_channel_server.domain.account.service.InvestAccountService;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class InvestAccountApiTest {

    @Mock
    private InvestAccountService investAccountService;

    @InjectMocks
    private InvestAccountApi investAccountApi;

    @Test
    @DisplayName("인증 정보 없으면 UNAUTHORIZED 예외 발생")
    void linkAccount_nullAuthenticatedUser_throwsUnauthorized() {
        // given
        LinkAccountRequest request = new LinkAccountRequest(UUID.randomUUID());

        // when & then
        assertThatThrownBy(() -> investAccountApi.linkAccount(null, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(AuthErrorCode.AUTHENTICATION_REQUIRED));
    }
}
