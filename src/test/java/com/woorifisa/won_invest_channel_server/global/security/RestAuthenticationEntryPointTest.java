package com.woorifisa.won_invest_channel_server.global.security;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RestAuthenticationEntryPointTest {

    private final RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint();

    @Test
    @DisplayName("미인증 요청에 401 ErrorResponse JSON 반환")
    void commence_returns401WithErrorResponseJson() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException authException = mock(AuthenticationException.class);

        // when
        entryPoint.commence(request, response, authException);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);

        String body = response.getContentAsString();
        assertThat(body).contains("\"status\":401");
        assertThat(body).contains("\"code\":\"AUTH_401_001\"");
        assertThat(body).contains("\"message\":\"인증이 필요합니다.\"");
    }
}
