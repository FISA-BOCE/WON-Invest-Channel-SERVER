package com.woorifisa.won_invest_channel_server.domain.account.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorifisa.won_invest_channel_server.domain.account.dto.request.InternalUpsertInvestAccountSummaryRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.response.InternalUpsertInvestAccountSummaryResponse;
import com.woorifisa.won_invest_channel_server.domain.account.service.InvestAccountService;
import com.woorifisa.won_invest_channel_server.global.config.SecurityConfig;
import com.woorifisa.won_invest_channel_server.global.security.InternalApiAuthFilter;
import com.woorifisa.won_invest_channel_server.global.security.JwtAuthenticationFilter;
import com.woorifisa.won_invest_channel_server.global.security.JwtTokenProvider;
import com.woorifisa.won_invest_channel_server.global.security.RestAccessDeniedHandler;
import com.woorifisa.won_invest_channel_server.global.security.RestAuthenticationEntryPoint;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = InvestInternalAccountSummaryApi.class)
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
class InvestInternalAccountSummaryApiTest {

    private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID INVEST_USER_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ACCOUNT_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InvestAccountService investAccountService;

    @Test
    @DisplayName("내부 인증 헤더가 없으면 summary upsert는 401을 반환한다")
    void upsertInternalAccountSummary_missingAuthHeader_unauthorized() throws Exception {
        InternalUpsertInvestAccountSummaryRequest request = new InternalUpsertInvestAccountSummaryRequest(
                INVEST_USER_UUID,
                USER_UUID,
                "123-***-***456",
                "ACTIVE"
        );

        mockMvc.perform(put("/internal/invest/accounts/summary")
                        .queryParam("investAccountUuid", ACCOUNT_UUID.toString())
                        .header("X-Service-ID", "won-card-channel")
                        .header("X-User-UUID", USER_UUID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("AUTH_401_001"));
    }

    @Test
    @DisplayName("내부 인증 헤더가 유효하면 summary upsert 결과를 반환한다")
    void upsertInternalAccountSummary_success() throws Exception {
        InternalUpsertInvestAccountSummaryRequest request = new InternalUpsertInvestAccountSummaryRequest(
                INVEST_USER_UUID,
                USER_UUID,
                "123-***-***456",
                "SUSPENDED"
        );
        given(investAccountService.upsertAccountSummary(eq(ACCOUNT_UUID), eq(request)))
                .willReturn(new InternalUpsertInvestAccountSummaryResponse(
                        ACCOUNT_UUID,
                        INVEST_USER_UUID,
                        USER_UUID,
                        "123-***-***456",
                        "SUSPENDED"
                ));

        mockMvc.perform(put("/internal/invest/accounts/summary")
                        .queryParam("investAccountUuid", ACCOUNT_UUID.toString())
                        .header("X-Service-ID", "won-card-channel")
                        .header("X-Internal-Api-Key", "internal-test-key")
                        .header("X-User-UUID", USER_UUID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("INVEST_200_006"))
                .andExpect(jsonPath("$.message").value("증권 계좌 summary 동기화가 완료되었습니다."))
                .andExpect(jsonPath("$.data.investAccountUuid").value(ACCOUNT_UUID.toString()))
                .andExpect(jsonPath("$.data.accountStatus").value("SUSPENDED"));
    }
}
