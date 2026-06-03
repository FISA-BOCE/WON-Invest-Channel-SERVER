package com.woorifisa.won_invest_channel_server.domain.invest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorifisa.won_invest_channel_server.domain.invest.dto.response.InvestEtfHoldingsResponse;
import com.woorifisa.won_invest_channel_server.domain.invest.service.InvestEtfQueryService;
import com.woorifisa.won_invest_channel_server.global.config.SecurityConfig;
import com.woorifisa.won_invest_channel_server.global.security.InternalApiAuthFilter;
import com.woorifisa.won_invest_channel_server.global.security.JwtAuthenticationFilter;
import com.woorifisa.won_invest_channel_server.global.security.JwtTokenProvider;
import com.woorifisa.won_invest_channel_server.global.security.RestAccessDeniedHandler;
import com.woorifisa.won_invest_channel_server.global.security.RestAuthenticationEntryPoint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = InvestEtfApi.class)
@Import({
        SecurityConfig.class,
        InternalApiAuthFilter.class,
        JwtAuthenticationFilter.class,
        JwtTokenProvider.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
@TestPropertySource(properties = {
        "app.security.jwt-secret=01234567890123456789012345678901",
        "app.security.access-token-expiration-seconds=3600",
        "internal.channel.allowed-service-id=internal-test-service",
        "internal.channel.service-id=internal-test-service",
        "internal.channel.api-key=internal-test-key"
})
class InvestEtfApiTest {

    private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ACCOUNT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private InvestEtfQueryService investEtfQueryService;

    @Test
    @DisplayName("인증된 요청이면 공통 응답 포맷으로 ETF 조회 결과를 반환한다")
    void getAccountEtfs_success() throws Exception {
        given(investEtfQueryService.getAccountEtfs(eq(USER_UUID), eq(ACCOUNT_UUID)))
                .willReturn(new InvestEtfHoldingsResponse(
                        new BigDecimal("79420.00"),
                        new BigDecimal("4820.00"),
                        new BigDecimal("6.45"),
                        List.of(new InvestEtfHoldingsResponse.Holding(
                                1L,
                                "S&P 500 ETF",
                                "VOO",
                                new BigDecimal("0.0235"),
                                new BigDecimal("375.40"),
                                new BigDecimal("79420.00"),
                                new BigDecimal("4820.00"),
                                new BigDecimal("6.45")
                        )),
                        List.of(new InvestEtfHoldingsResponse.RecentExecution(
                                LocalDateTime.parse("2026-05-16T00:00:00"),
                                "VOO",
                                new BigDecimal("0.0235"),
                                "시장가 체결"
                        ))
                ));

        mockMvc.perform(get("/api/invest/accounts/{accountUuid}/etfs", ACCOUNT_UUID)
                        .header("X-Service-ID", "WOORI-FISA-APP-01")
                        .header("X-Transaction-ID", "TX-20260512-INV01")
                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("INVEST_200_005"))
                .andExpect(jsonPath("$.message").value("보유 ETF 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.totalEvaluationAmount").value(79420.00))
                .andExpect(jsonPath("$.data.holdings[0].ticker").value("VOO"))
                .andExpect(jsonPath("$.data.recentExecutions[0].executionType").value("시장가 체결"));
    }

    @Test
    @DisplayName("인증 없는 요청이면 401을 반환한다")
    void getAccountEtfs_unauthorized() throws Exception {
        mockMvc.perform(get("/api/invest/accounts/{accountUuid}/etfs", ACCOUNT_UUID)
                        .header("X-Service-ID", "WOORI-FISA-APP-01")
                        .header("X-Transaction-ID", "TX-20260512-INV01"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("AUTH_401_001"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    private String bearerToken() {
        return "Bearer " + jwtTokenProvider.generateAccessToken(USER_UUID, USER_UUID, "jti-test");
    }
}
