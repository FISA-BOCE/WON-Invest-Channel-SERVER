package com.woorifisa.won_invest_channel_server.domain.account.api;

import com.woorifisa.won_invest_channel_server.domain.account.dto.response.InternalInvestAccountsResponse;
import com.woorifisa.won_invest_channel_server.domain.account.service.InvestAccountService;
import com.woorifisa.won_invest_channel_server.global.config.SecurityConfig;
import com.woorifisa.won_invest_channel_server.global.security.InternalApiAuthFilter;
import com.woorifisa.won_invest_channel_server.global.security.JwtAuthenticationFilter;
import com.woorifisa.won_invest_channel_server.global.security.JwtTokenProvider;
import com.woorifisa.won_invest_channel_server.global.security.RestAccessDeniedHandler;
import com.woorifisa.won_invest_channel_server.global.security.RestAuthenticationEntryPoint;
import java.util.List;
import java.util.UUID;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = InvestInternalAccountApi.class)
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
class InvestInternalAccountApiTest {

    private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ACCOUNT_UUID_1 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ACCOUNT_UUID_2 = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvestAccountService investAccountService;

    @Test
    @DisplayName("내부 인증 헤더가 없으면 401을 반환한다")
    void getInternalAccounts_missingAuthHeader_unauthorized() throws Exception {
        mockMvc.perform(get("/internal/invest/accounts")
                        .header("X-Service-ID", "internal-test-service")
                        .header("X-User-UUID", USER_UUID.toString()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("AUTH_401_001"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    @DisplayName("내부 인증 헤더가 일치하지 않으면 401을 반환한다")
    void getInternalAccounts_invalidAuthHeader_unauthorized() throws Exception {
        mockMvc.perform(get("/internal/invest/accounts")
                        .header("X-Service-ID", "internal-test-service")
                        .header("X-Internal-Api-Key", "wrong-key")
                        .header("X-User-UUID", USER_UUID.toString()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("AUTH_401_001"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    @DisplayName("계좌가 없으면 200과 빈 배열을 반환한다")
    void getInternalAccounts_emptyAccounts_success() throws Exception {
        given(investAccountService.getInternalAccounts(eq(USER_UUID)))
                .willReturn(new InternalInvestAccountsResponse(List.of()));

        mockMvc.perform(get("/internal/invest/accounts")
                        .header("X-Service-ID", "internal-test-service")
                        .header("X-Internal-Api-Key", "internal-test-key")
                        .header("X-User-UUID", USER_UUID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("INVEST_200_005"))
                .andExpect(jsonPath("$.message").value("증권 계좌 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.accounts").isArray())
                .andExpect(jsonPath("$.data.accounts").isEmpty());
    }

    @Test
    @DisplayName("계좌가 여러 건이면 상태값을 포함한 배열을 반환한다")
    void getInternalAccounts_multipleAccounts_success() throws Exception {
        given(investAccountService.getInternalAccounts(eq(USER_UUID)))
                .willReturn(new InternalInvestAccountsResponse(List.of(
                        new InternalInvestAccountsResponse.Account(
                                ACCOUNT_UUID_1,
                                "123-***-***456",
                                "ACTIVE"
                        ),
                        new InternalInvestAccountsResponse.Account(
                                ACCOUNT_UUID_2,
                                "987-***-***654",
                                "SUSPENDED"
                        )
                )));

        mockMvc.perform(get("/internal/invest/accounts")
                        .header("X-Service-ID", "internal-test-service")
                        .header("X-Internal-Api-Key", "internal-test-key")
                        .header("X-User-UUID", USER_UUID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("INVEST_200_005"))
                .andExpect(jsonPath("$.data.accounts[0].investAccountUuid").value(ACCOUNT_UUID_1.toString()))
                .andExpect(jsonPath("$.data.accounts[0].accountNoDisplay").value("123-***-***456"))
                .andExpect(jsonPath("$.data.accounts[0].accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.accounts[1].investAccountUuid").value(ACCOUNT_UUID_2.toString()))
                .andExpect(jsonPath("$.data.accounts[1].accountStatus").value("SUSPENDED"));
    }
}
