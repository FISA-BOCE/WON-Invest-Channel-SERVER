package com.woorifisa.won_invest_channel_server.domain.invest.api;

import com.woorifisa.won_invest_channel_server.domain.invest.dto.response.InvestMainResponse;
import com.woorifisa.won_invest_channel_server.domain.invest.service.InvestMainQueryService;
import com.woorifisa.won_invest_channel_server.global.config.SecurityConfig;
import com.woorifisa.won_invest_channel_server.global.security.InternalApiAuthFilter;
import com.woorifisa.won_invest_channel_server.global.security.JwtAuthenticationFilter;
import com.woorifisa.won_invest_channel_server.global.security.JwtTokenProvider;
import com.woorifisa.won_invest_channel_server.global.security.RestAccessDeniedHandler;
import com.woorifisa.won_invest_channel_server.global.security.RestAuthenticationEntryPoint;
import java.math.BigDecimal;
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
@WebMvcTest(controllers = InvestMainApi.class)
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
        "internal.allowed-service-ids=won-card-channel,won-common",
        "internal.service-id=won-invest-channel",
        "internal.api-key=internal-test-key",
        "internal.services.invest-core.base-url=http://localhost:18081",
        "internal.services.common.base-url=http://localhost:18082"
})
class InvestMainApiTest {

    private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ACCOUNT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private InvestMainQueryService investMainQueryService;

    @Test
    @DisplayName("인증된 요청이면 투자 메인 화면 응답을 공통 포맷으로 반환한다")
    void getInvestMain_success() throws Exception {
        given(investMainQueryService.getInvestMain(eq(USER_UUID)))
                .willReturn(new InvestMainResponse(
                        new BigDecimal("79420.00"),
                        new BigDecimal("4820.00"),
                        new BigDecimal("6.45"),
                        new InvestMainResponse.Account(
                                ACCOUNT_UUID,
                                "123-***-***456",
                                "홍*동"
                        ),
                        new InvestMainResponse.CashBalance(
                                BigDecimal.ZERO,
                                "전액 환전·매수 완료",
                                BigDecimal.ZERO,
                                BigDecimal.ZERO
                        ),
                        List.of(
                                new InvestMainResponse.RecentPayment(
                                        "나스닥 100 ETF",
                                        "QQQ",
                                        new BigDecimal("0.0241"),
                                        new BigDecimal("16280.00")
                                ),
                                new InvestMainResponse.RecentPayment(
                                        "S&P 500 ETF",
                                        "VOO",
                                        new BigDecimal("0.0132"),
                                        new BigDecimal("11800.00")
                                )
                        )
                ));

        mockMvc.perform(get("/api/invest/main")
                        .header("X-Transaction-ID", "TX-20260609-INVMAIN01")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("INVEST_200_009"))
                .andExpect(jsonPath("$.message").value("투자 자산 요약 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.totalEvaluationAmount").value(79420.00))
                .andExpect(jsonPath("$.data.account.investAccountUuid").value(ACCOUNT_UUID.toString()))
                .andExpect(jsonPath("$.data.account.accountHolderName").value("홍*동"))
                .andExpect(jsonPath("$.data.cashBalance.krwStatus").value("전액 환전·매수 완료"))
                .andExpect(jsonPath("$.data.recentPayments[0].ticker").value("QQQ"));
    }

    @Test
    @DisplayName("인증 없는 요청이면 401을 반환한다")
    void getInvestMain_unauthorized() throws Exception {
        mockMvc.perform(get("/api/invest/main"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("AUTH_401_001"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    private String bearerToken() {
        return "Bearer " + jwtTokenProvider.generateAccessToken(USER_UUID, USER_UUID, "jti-test");
    }
}
