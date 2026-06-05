package com.woorifisa.won_invest_channel_server.domain.aidb.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.woorifisa.won_invest_channel_server.domain.aidb.dto.request.AiDbQueryRequest;
import com.woorifisa.won_invest_channel_server.domain.aidb.dto.response.AiDbHoldingListResponse;
import com.woorifisa.won_invest_channel_server.domain.aidb.dto.response.AiDbHoldingSummaryResponse;
import com.woorifisa.won_invest_channel_server.domain.aidb.exception.code.AiDbErrorCode;
import com.woorifisa.won_invest_channel_server.domain.aidb.repository.InvestChnAiSummaryRepository;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataRetrievalFailureException;

@ExtendWith(MockitoExtension.class)
class AiDbQueryServiceTest {

    private static final UUID USER_UUID = UUID.fromString("8976c015-14e7-4c82-8817-978434d353dc");
    private static final UUID ACCOUNT_UUID = UUID.fromString("a1111111-2222-3333-4444-555555555555");
    private static final LocalDateTime SYNCED_AT = LocalDateTime.of(2026, 6, 5, 10, 30);

    @Mock
    private InvestChnAiSummaryRepository investChnAiSummaryRepository;

    @InjectMocks
    private AiDbQueryService aiDbQueryService;

    @Test
    @DisplayName("MY_ETF_HOLDINGS 요청이면 보유 ETF 목록 응답을 반환한다")
    void query_myEtfHoldings_success() {
        given(investChnAiSummaryRepository.findHoldingListResponseByUserUuid(USER_UUID))
                .willReturn(Optional.of(holdingListResponse()));

        Object response = aiDbQueryService.query(new AiDbQueryRequest(USER_UUID, "MY_ETF_HOLDINGS"));

        assertThat(response).isInstanceOf(AiDbHoldingListResponse.class);
        AiDbHoldingListResponse result = (AiDbHoldingListResponse) response;
        assertThat(result.userUuid()).isEqualTo(USER_UUID);
        assertThat(result.investAccountUuid()).isEqualTo(ACCOUNT_UUID);
        assertThat(result.holdingCount()).isEqualTo(2);
        assertThat(result.holdings()).hasSize(2);
        assertThat(result.holdings().get(0).ticker()).isEqualTo("TIGER500");
    }

    @Test
    @DisplayName("MY_ETF_BALANCE_SUMMARY 요청이면 AI 요약 응답을 반환한다")
    void query_myEtfBalanceSummary_success() {
        given(investChnAiSummaryRepository.findHoldingSummaryResponseByUserUuid(USER_UUID))
                .willReturn(Optional.of(holdingSummaryResponse()));

        Object response = aiDbQueryService.query(new AiDbQueryRequest(USER_UUID, "MY_ETF_BALANCE_SUMMARY"));

        assertThat(response).isInstanceOf(AiDbHoldingSummaryResponse.class);
        AiDbHoldingSummaryResponse result = (AiDbHoldingSummaryResponse) response;
        assertThat(result.userUuid()).isEqualTo(USER_UUID);
        assertThat(result.investAccountUuid()).isEqualTo(ACCOUNT_UUID);
        assertThat(result.totalBuyAmount()).isEqualByComparingTo("150000.0000");
        assertThat(result.totalEvaluationAmount()).isEqualByComparingTo("168456.7800");
        assertThat(result.totalProfitLossAmount()).isEqualByComparingTo("18456.7800");
        assertThat(result.totalProfitLossRate()).isEqualByComparingTo("0.123045");
        assertThat(result.holdingCount()).isEqualTo(2);
        assertThat(result.holdings()).hasSize(2);
        assertThat(result.holdings().get(0).averageBuyPrice()).isEqualByComparingTo("90000.0000");
    }

    @Test
    @DisplayName("지원하지 않는 queryType이면 CHAT_400_003 예외를 발생시킨다")
    void query_unsupportedQueryType() {
        assertThatThrownBy(() -> aiDbQueryService.query(new AiDbQueryRequest(USER_UUID, "UNKNOWN")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(AiDbErrorCode.UNSUPPORTED_QUERY_TYPE));
    }

    @Test
    @DisplayName("조회 결과가 없으면 CHAT_404_001 예외를 발생시킨다")
    void query_resultNotFound() {
        given(investChnAiSummaryRepository.findHoldingListResponseByUserUuid(USER_UUID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> aiDbQueryService.query(new AiDbQueryRequest(USER_UUID, "MY_ETF_HOLDINGS")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(AiDbErrorCode.QUERY_RESULT_NOT_FOUND));
    }

    @Test
    @DisplayName("DB 조회 오류가 발생하면 CHAT_500_001 예외를 발생시킨다")
    void query_mysqlQueryFailed() {
        DataRetrievalFailureException cause = new DataRetrievalFailureException("query failed");
        given(investChnAiSummaryRepository.findHoldingSummaryResponseByUserUuid(USER_UUID))
                .willThrow(cause);

        assertThatThrownBy(() -> aiDbQueryService.query(new AiDbQueryRequest(USER_UUID, "MY_ETF_BALANCE_SUMMARY")))
                .isInstanceOf(BusinessException.class)
                .hasCause(cause)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(AiDbErrorCode.MYSQL_QUERY_FAILED));
    }

    private AiDbHoldingListResponse holdingListResponse() {
        return new AiDbHoldingListResponse(
                USER_UUID,
                ACCOUNT_UUID,
                2,
                List.of(
                        new AiDbHoldingListResponse.Holding(
                                1L,
                                "TIGER500",
                                "TIGER500 ETF",
                                new BigDecimal("1.23456789"),
                                new BigDecimal("123456.7800"),
                                SYNCED_AT
                        ),
                        new AiDbHoldingListResponse.Holding(
                                2L,
                                "KODEX200",
                                "KODEX200 ETF",
                                new BigDecimal("1.23456789"),
                                new BigDecimal("123456.7800"),
                                SYNCED_AT
                        )
                )
        );
    }

    private AiDbHoldingSummaryResponse holdingSummaryResponse() {
        return new AiDbHoldingSummaryResponse(
                USER_UUID,
                ACCOUNT_UUID,
                new BigDecimal("150000.0000"),
                new BigDecimal("168456.7800"),
                new BigDecimal("18456.7800"),
                new BigDecimal("0.123045"),
                2,
                SYNCED_AT,
                List.of(
                        new AiDbHoldingSummaryResponse.Holding(
                                1L,
                                "TIGER500",
                                "TIGER500 ETF",
                                new BigDecimal("1.23456789"),
                                new BigDecimal("90000.0000"),
                                new BigDecimal("123456.7800"),
                                new BigDecimal("13456.7800"),
                                new BigDecimal("0.122334"),
                                SYNCED_AT
                        ),
                        new AiDbHoldingSummaryResponse.Holding(
                                2L,
                                "KODEX200",
                                "KODEX200 ETF",
                                new BigDecimal("1.23456789"),
                                new BigDecimal("90000.0000"),
                                new BigDecimal("123456.7800"),
                                new BigDecimal("13456.7800"),
                                new BigDecimal("0.122334"),
                                SYNCED_AT
                        )
                )
        );
    }
}
