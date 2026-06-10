package com.woorifisa.won_invest_channel_server.domain.invest.api;

import com.woorifisa.won_invest_channel_server.domain.invest.dto.request.InvestAutoInvestExecutionHistoryQuery;
import com.woorifisa.won_invest_channel_server.domain.invest.dto.response.InvestAutoInvestExecutionHistoryResponse;
import com.woorifisa.won_invest_channel_server.domain.invest.dto.response.InvestEtfHoldingsResponse;
import com.woorifisa.won_invest_channel_server.domain.invest.service.InvestEtfQueryService;
import com.woorifisa.won_invest_channel_server.global.config.SecurityConfig;
import com.woorifisa.won_invest_channel_server.global.security.InternalApiAuthFilter;
import com.woorifisa.won_invest_channel_server.global.security.JwtAuthenticationFilter;
import com.woorifisa.won_invest_channel_server.global.security.JwtTokenProvider;
import com.woorifisa.won_invest_channel_server.global.security.RestAccessDeniedHandler;
import com.woorifisa.won_invest_channel_server.global.security.RestAuthenticationEntryPoint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
@WebMvcTest(controllers = {InvestEtfApi.class, InvestInternalEtfApi.class})
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
                        LocalDate.of(2026, 6, 4),
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
                                OffsetDateTime.parse("2026-05-16T00:00:00+09:00"),
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
                .andExpect(jsonPath("$.code").value("INVEST_200_007"))
                .andExpect(jsonPath("$.message").value("보유 ETF 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.baseDate").value("2026-06-04"))
                .andExpect(jsonPath("$.data.totalEvaluationAmount").value(79420.00))
                .andExpect(jsonPath("$.data.holdings[0].ticker").value("VOO"))
                .andExpect(jsonPath("$.data.recentExecutions[0].executionType").value("시장가 체결"));
    }

    @Test
    @DisplayName("인증된 요청이면 공통 응답 포맷으로 자동 투자 체결 이력을 반환한다")
    void getAutoInvestExecutionHistories_success() throws Exception {
        given(investEtfQueryService.getAutoInvestExecutionHistories(
                eq(USER_UUID),
                eq(ACCOUNT_UUID),
                eq(new InvestAutoInvestExecutionHistoryQuery(
                        OffsetDateTime.parse("2026-06-01T00:00:00+09:00"),
                        OffsetDateTime.parse("2026-06-11T23:59:59+09:00"),
                        "COMPLETED",
                        "VOO",
                        null,
                        20
                ))
        )).willReturn(autoInvestResponse());

        mockMvc.perform(get("/api/invest/accounts/{accountUuid}/auto-invest/executions", ACCOUNT_UUID)
                        .header("X-Service-ID", "WOORI-FISA-APP-01")
                        .header("X-Transaction-ID", "TX-20260512-INV02")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .queryParam("from", "2026-06-01T00:00:00+09:00")
                        .queryParam("to", "2026-06-11T23:59:59+09:00")
                        .queryParam("status", "COMPLETED")
                        .queryParam("ticker", "VOO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("INVEST_200_010"))
                .andExpect(jsonPath("$.message").value("ETF 자동 투자 체결 이력 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.histories[0].ticker").value("VOO"))
                .andExpect(jsonPath("$.data.histories[0].executionStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.hasNext").value(true));
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

    @Test
    @DisplayName("자동 투자 체결 이력 조회 시 size가 100보다 크면 400을 반환한다")
    void getAutoInvestExecutionHistories_invalidSize() throws Exception {
        mockMvc.perform(get("/api/invest/accounts/{accountUuid}/auto-invest/executions", ACCOUNT_UUID)
                        .header("X-Service-ID", "WOORI-FISA-APP-01")
                        .header("X-Transaction-ID", "TX-20260512-INV03")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .queryParam("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COM_400_002"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 요청값입니다."));
    }

    @Test
    @DisplayName("내부 API 요청에 X-User-UUID가 없으면 401을 반환한다")
    void getInternalAccountEtfs_missingUserUuidHeader() throws Exception {
        mockMvc.perform(get("/internal/invest/accounts/{accountUuid}/etfs", ACCOUNT_UUID)
                        .header("X-Service-ID", "won-card-channel")
                        .header("X-Internal-Api-Key", "internal-test-key"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("AUTH_401_001"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    @DisplayName("내부 API 인증 헤더가 유효하면 공통 응답 포맷으로 ETF 조회 결과를 반환한다")
    void getInternalAccountEtfs_success() throws Exception {
        given(investEtfQueryService.getAccountEtfs(eq(USER_UUID), eq(ACCOUNT_UUID)))
                .willReturn(new InvestEtfHoldingsResponse(
                        LocalDate.of(2026, 6, 4),
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
                        List.of()
                ));

        mockMvc.perform(get("/internal/invest/accounts/{accountUuid}/etfs", ACCOUNT_UUID)
                        .header("X-Service-ID", "won-card-channel")
                        .header("X-Internal-Api-Key", "internal-test-key")
                        .header("X-User-UUID", USER_UUID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("INVEST_200_007"))
                .andExpect(jsonPath("$.data.holdings[0].ticker").value("VOO"));
    }

    @Test
    @DisplayName("내부 API 인증 헤더가 유효하면 공통 응답 포맷으로 자동 투자 체결 이력을 반환한다")
    void getInternalAutoInvestExecutionHistories_success() throws Exception {
        given(investEtfQueryService.getAutoInvestExecutionHistories(
                eq(USER_UUID),
                eq(ACCOUNT_UUID),
                eq(new InvestAutoInvestExecutionHistoryQuery(null, null, null, null, "cursor-1", 10))
        )).willReturn(autoInvestResponse());

        mockMvc.perform(get("/internal/invest/accounts/{accountUuid}/auto-invest/executions", ACCOUNT_UUID)
                        .header("X-Service-ID", "won-card-channel")
                        .header("X-Internal-Api-Key", "internal-test-key")
                        .header("X-User-UUID", USER_UUID.toString())
                        .queryParam("cursor", "cursor-1")
                        .queryParam("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("INVEST_200_010"))
                .andExpect(jsonPath("$.data.histories[0].ticker").value("VOO"));
    }

    private String bearerToken() {
        return "Bearer " + jwtTokenProvider.generateAccessToken(USER_UUID, USER_UUID, "jti-test");
    }

    private InvestAutoInvestExecutionHistoryResponse autoInvestResponse() {
        return new InvestAutoInvestExecutionHistoryResponse(
                OffsetDateTime.parse("2026-06-11T10:30:00+09:00"),
                List.of(new InvestAutoInvestExecutionHistoryResponse.History(
                        12031L,
                        882193L,
                        1L,
                        "Vanguard S&P 500 ETF",
                        "VOO",
                        "COMPLETED",
                        new BigDecimal("15000.00"),
                        new BigDecimal("0.02730000"),
                        new BigDecimal("0.02730000"),
                        new BigDecimal("549.1200"),
                        new BigDecimal("14982.3300"),
                        OffsetDateTime.parse("2026-06-10T22:00:00+09:00"),
                        OffsetDateTime.parse("2026-06-10T22:00:03+09:00"),
                        null,
                        null
                )),
                "2026-06-10T22:00:03+09:00|12031",
                true
        );
    }
}
