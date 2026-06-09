package com.woorifisa.won_invest_channel_server.domain.etf.api;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.InternalInvestEtfDetailResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.exception.code.EtfErrorCode;
import com.woorifisa.won_invest_channel_server.domain.etf.service.InvestEtfProductQueryService;
import com.woorifisa.won_invest_channel_server.global.config.SecurityConfig;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = InvestInternalEtfProductApi.class)
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
class InvestInternalEtfProductApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvestEtfProductQueryService investEtfProductQueryService;

    @Test
    @DisplayName("내부 ETF 단건 조회 성공 시 카드 채널 검증용 필드를 반환한다")
    void getInternalEtfProductDetail_success() throws Exception {
        given(investEtfProductQueryService.getInternalEtfProductDetail(eq(1L)))
                .willReturn(new InternalInvestEtfDetailResponse(
                        1L,
                        "Vanguard S&P 500 ETF",
                        "VOO",
                        true,
                        true
                ));

        mockMvc.perform(get("/internal/invest/etfs/{etfId}", 1L)
                        .header("X-Service-ID", "won-card-channel")
                        .header("X-Internal-Api-Key", "internal-test-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("INVEST_200_004"))
                .andExpect(jsonPath("$.message").value("ETF 상품 상세 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.etfId").value(1L))
                .andExpect(jsonPath("$.data.etfName").value("Vanguard S&P 500 ETF"))
                .andExpect(jsonPath("$.data.ticker").value("VOO"))
                .andExpect(jsonPath("$.data.isTradeAvailable").value(true))
                .andExpect(jsonPath("$.data.isFractionalAvailable").value(true));
    }

    @Test
    @DisplayName("내부 ETF 단건 조회 시 ETF가 없으면 404를 반환한다")
    void getInternalEtfProductDetail_notFound() throws Exception {
        willThrow(new BusinessException(EtfErrorCode.ETF_PRODUCT_NOT_FOUND))
                .given(investEtfProductQueryService)
                .getInternalEtfProductDetail(eq(999L));

        mockMvc.perform(get("/internal/invest/etfs/{etfId}", 999L)
                        .header("X-Service-ID", "won-card-channel")
                        .header("X-Internal-Api-Key", "internal-test-key"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("ETF_404_001"));
    }

    @Test
    @DisplayName("내부 ETF 단건 조회 시 인증 정보가 없으면 401을 반환한다")
    void getInternalEtfProductDetail_unauthorized() throws Exception {
        mockMvc.perform(get("/internal/invest/etfs/{etfId}", 1L)
                        .header("X-Service-ID", "won-card-channel"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("AUTH_401_001"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    @DisplayName("내부 ETF 단건 조회 시 etfId가 0 이하면 400을 반환한다")
    void getInternalEtfProductDetail_invalidEtfId() throws Exception {
        mockMvc.perform(get("/internal/invest/etfs/{etfId}", 0L)
                        .header("X-Service-ID", "won-card-channel")
                        .header("X-Internal-Api-Key", "internal-test-key"))
                .andExpect(status().isBadRequest());
    }
}
