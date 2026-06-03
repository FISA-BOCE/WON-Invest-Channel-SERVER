package com.woorifisa.won_invest_channel_server.domain.etf.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.InvestEtfProductDetailResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfRiskGrade;
import com.woorifisa.won_invest_channel_server.domain.etf.service.InvestEtfProductQueryService;
import com.woorifisa.won_invest_channel_server.global.config.SecurityConfig;
import com.woorifisa.won_invest_channel_server.global.security.InternalApiAuthFilter;
import com.woorifisa.won_invest_channel_server.global.security.JwtAuthenticationFilter;
import com.woorifisa.won_invest_channel_server.global.security.JwtTokenProvider;
import com.woorifisa.won_invest_channel_server.global.security.RestAccessDeniedHandler;
import com.woorifisa.won_invest_channel_server.global.security.RestAuthenticationEntryPoint;
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

import java.util.UUID;

@ActiveProfiles("test")
@WebMvcTest(controllers = InvestEtfProductApi.class)
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
class InvestEtfProductApiTest {

    private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private InvestEtfProductQueryService investEtfProductQueryService;

    @Test
    @DisplayName("인증된 요청이면 ETF 상세 조회 결과를 공통 응답 포맷으로 반환한다")
    void getEtfProductDetail_success() throws Exception {
        given(investEtfProductQueryService.getEtfProductDetail(eq(1L)))
                .willReturn(new InvestEtfProductDetailResponse(
                        1L,
                        "Vanguard S&P 500 ETF",
                        "VOO",
                        "AMEX",
                        com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency.USD,
                        EtfRiskGrade.MEDIUM
                ));

        mockMvc.perform(get("/api/invest/etfs/{etfId}", 1L)
                        .header("X-Service-ID", "WOORI-FISA-APP-01")
                        .header("X-Transaction-ID", "TX-20260604-ETF01")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("INVEST_200_004"))
                .andExpect(jsonPath("$.message").value("ETF 상품 상세 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.etfId").value(1))
                .andExpect(jsonPath("$.data.etfName").value("Vanguard S&P 500 ETF"))
                .andExpect(jsonPath("$.data.ticker").value("VOO"))
                .andExpect(jsonPath("$.data.market").value("AMEX"))
                .andExpect(jsonPath("$.data.currency").value("USD"))
                .andExpect(jsonPath("$.data.riskGrade").value("MEDIUM"));
    }

    @Test
    @DisplayName("인증 없는 요청이면 401을 반환한다")
    void getEtfProductDetail_unauthorized() throws Exception {
        mockMvc.perform(get("/api/invest/etfs/{etfId}", 1L)
                        .header("X-Service-ID", "WOORI-FISA-APP-01")
                        .header("X-Transaction-ID", "TX-20260604-ETF01"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("AUTH_401_001"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    private String bearerToken() {
        return "Bearer " + jwtTokenProvider.generateAccessToken(USER_UUID, USER_UUID, "jti-test");
    }
}
