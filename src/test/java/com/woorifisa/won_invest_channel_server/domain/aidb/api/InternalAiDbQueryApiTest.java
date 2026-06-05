package com.woorifisa.won_invest_channel_server.domain.aidb.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.woorifisa.won_invest_channel_server.domain.aidb.dto.request.AiDbQueryRequest;
import com.woorifisa.won_invest_channel_server.domain.aidb.dto.response.AiDbHoldingListResponse;
import com.woorifisa.won_invest_channel_server.domain.aidb.service.AiDbQueryService;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class InternalAiDbQueryApiTest {

    private static final UUID USER_UUID = UUID.fromString("8976c015-14e7-4c82-8817-978434d353dc");
    private static final UUID ACCOUNT_UUID = UUID.fromString("a1111111-2222-3333-4444-555555555555");

    @Mock
    private AiDbQueryService aiDbQueryService;

    @InjectMocks
    private InternalAiDbQueryApi internalAiDbQueryApi;

    @Test
    @DisplayName("AI DB 조회 결과를 공통 성공 응답으로 반환한다")
    void query_success() {
        AiDbQueryRequest request = new AiDbQueryRequest(USER_UUID, "MY_ETF_HOLDINGS");
        AiDbHoldingListResponse serviceResponse = new AiDbHoldingListResponse(
                USER_UUID,
                ACCOUNT_UUID,
                1,
                List.of(new AiDbHoldingListResponse.Holding(
                        1L,
                        "TIGER500",
                        "TIGER 미국S&P500",
                        new BigDecimal("1.23456789"),
                        new BigDecimal("123456.7800"),
                        LocalDateTime.of(2026, 6, 5, 10, 30)
                ))
        );
        given(aiDbQueryService.query(request)).willReturn(serviceResponse);

        ResponseEntity<ApiResponse<Object>> response =
                internalAiDbQueryApi.query("ai-service", request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(200);
        assertThat(response.getBody().code()).isEqualTo("COM_200_001");
        assertThat(response.getBody().data()).isEqualTo(serviceResponse);
    }
}
