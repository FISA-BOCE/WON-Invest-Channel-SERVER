package com.woorifisa.won_invest_channel_server.domain.invest.service;

import com.woorifisa.won_invest_channel_server.domain.account.model.AccountStatus;
import com.woorifisa.won_invest_channel_server.domain.account.model.InvestChnAccountSummary;
import com.woorifisa.won_invest_channel_server.domain.account.repository.InvestChnAccountSummaryRepository;
import com.woorifisa.won_invest_channel_server.domain.invest.dto.request.InvestAutoInvestExecutionHistoryQuery;
import com.woorifisa.won_invest_channel_server.domain.invest.dto.response.InvestAutoInvestExecutionHistoryResponse;
import com.woorifisa.won_invest_channel_server.domain.invest.dto.response.InvestEtfHoldingsResponse;
import com.woorifisa.won_invest_channel_server.domain.invest.exception.code.InvestErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class InvestEtfQueryServiceTest {

    private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_USER_UUID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID ACCOUNT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private InvestChnAccountSummaryRepository accountSummaryRepository;

    @Mock
    private InvestCoreEtfQueryClient investCoreEtfQueryClient;

    @InjectMocks
    private InvestEtfQueryService investEtfQueryService;

    @Test
    @DisplayName("정상 조회 시 총 평가금액, 손익, 수익률, 최근 매수 3건을 반환한다")
    void getAccountEtfs_success() {
        InvestChnAccountSummary account = account(AccountStatus.ACTIVE, USER_UUID);
        InvestEtfHoldingsResponse coreData = response();
        given(accountSummaryRepository.findById(ACCOUNT_UUID)).willReturn(Optional.of(account));
        given(investCoreEtfQueryClient.fetchCoreEtfHoldings(USER_UUID, ACCOUNT_UUID)).willReturn(coreData);

        InvestEtfHoldingsResponse response = investEtfQueryService.getAccountEtfs(USER_UUID, ACCOUNT_UUID);

        assertThat(response).isSameAs(coreData);
        assertThat(response.totalEvaluationAmount()).isEqualByComparingTo("79420.00");
        assertThat(response.recentExecutions()).hasSize(3);
        assertThat(response.recentExecutions().get(0).executionType()).isEqualTo("시장가 체결");
    }

    @Test
    @DisplayName("계좌가 없으면 ACCOUNT_NOT_FOUND 예외가 발생한다")
    void getAccountEtfs_accountNotFound() {
        given(accountSummaryRepository.findById(ACCOUNT_UUID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> investEtfQueryService.getAccountEtfs(USER_UUID, ACCOUNT_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestErrorCode.ACCOUNT_NOT_FOUND));
        then(investCoreEtfQueryClient).should(never()).fetchCoreEtfHoldings(USER_UUID, ACCOUNT_UUID);
    }

    @Test
    @DisplayName("본인 계좌가 아니면 ACCOUNT_NOT_OWNER 예외가 발생한다")
    void getAccountEtfs_notOwner() {
        given(accountSummaryRepository.findById(ACCOUNT_UUID))
                .willReturn(Optional.of(account(AccountStatus.ACTIVE, OTHER_USER_UUID)));

        assertThatThrownBy(() -> investEtfQueryService.getAccountEtfs(USER_UUID, ACCOUNT_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestErrorCode.ACCOUNT_NOT_OWNER));
        then(investCoreEtfQueryClient).should(never()).fetchCoreEtfHoldings(USER_UUID, ACCOUNT_UUID);
    }

    @Test
    @DisplayName("비활성 계좌면 INVALID_ACCOUNT_STATUS 예외가 발생한다")
    void getAccountEtfs_inactiveAccount() {
        given(accountSummaryRepository.findById(ACCOUNT_UUID))
                .willReturn(Optional.of(account(AccountStatus.SUSPENDED, USER_UUID)));

        assertThatThrownBy(() -> investEtfQueryService.getAccountEtfs(USER_UUID, ACCOUNT_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestErrorCode.INVALID_ACCOUNT_STATUS));
        then(investCoreEtfQueryClient).should(never()).fetchCoreEtfHoldings(USER_UUID, ACCOUNT_UUID);
    }

    @Test
    @DisplayName("자동 투자 체결 이력 정상 조회 시 Core 응답을 그대로 반환한다")
    void getAutoInvestExecutionHistories_success() {
        InvestChnAccountSummary account = account(AccountStatus.ACTIVE, USER_UUID);
        InvestAutoInvestExecutionHistoryQuery query = new InvestAutoInvestExecutionHistoryQuery(
                OffsetDateTime.parse("2026-06-01T00:00:00+09:00"),
                OffsetDateTime.parse("2026-06-11T23:59:59+09:00"),
                "COMPLETED",
                "VOO",
                null,
                20
        );
        InvestAutoInvestExecutionHistoryResponse coreData = autoInvestResponse();
        given(accountSummaryRepository.findById(ACCOUNT_UUID)).willReturn(Optional.of(account));
        given(investCoreEtfQueryClient.fetchAutoInvestExecutionHistories(USER_UUID, ACCOUNT_UUID, query))
                .willReturn(coreData);

        InvestAutoInvestExecutionHistoryResponse response =
                investEtfQueryService.getAutoInvestExecutionHistories(USER_UUID, ACCOUNT_UUID, query);

        assertThat(response).isSameAs(coreData);
        assertThat(response.histories()).hasSize(1);
        assertThat(response.histories().get(0).executionStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("자동 투자 체결 이력 조회에서 from이 to보다 늦으면 INVALID_AUTO_INVEST_EXECUTION_QUERY 예외가 발생한다")
    void getAutoInvestExecutionHistories_invalidRange() {
        InvestAutoInvestExecutionHistoryQuery query = new InvestAutoInvestExecutionHistoryQuery(
                OffsetDateTime.parse("2026-06-12T00:00:00+09:00"),
                OffsetDateTime.parse("2026-06-11T00:00:00+09:00"),
                null,
                null,
                null,
                20
        );
        given(accountSummaryRepository.findById(ACCOUNT_UUID))
                .willReturn(Optional.of(account(AccountStatus.ACTIVE, USER_UUID)));

        assertThatThrownBy(() -> investEtfQueryService.getAutoInvestExecutionHistories(USER_UUID, ACCOUNT_UUID, query))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestErrorCode.INVALID_AUTO_INVEST_EXECUTION_QUERY));
        then(investCoreEtfQueryClient).should(never()).fetchAutoInvestExecutionHistories(USER_UUID, ACCOUNT_UUID, query);
    }

    private InvestChnAccountSummary account(AccountStatus accountStatus, UUID userUuid) {
        return InvestChnAccountSummary.builder()
                .investAccountUuid(ACCOUNT_UUID)
                .investUserUuid(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                .userUuid(userUuid)
                .accountNoDisplay("100-200-300")
                .accountStatus(accountStatus)
                .build();
    }

    private InvestEtfHoldingsResponse response() {
        return new InvestEtfHoldingsResponse(
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
                List.of(
                        execution("2026-05-16T00:00:00+09:00", "VOO", "0.0235"),
                        execution("2026-05-15T00:00:00+09:00", "QQQ", "0.0100"),
                        execution("2026-05-14T00:00:00+09:00", "VOO", "0.0050")
                )
        );
    }

    private InvestEtfHoldingsResponse.RecentExecution execution(String executedAt, String ticker, String quantity) {
        return new InvestEtfHoldingsResponse.RecentExecution(
                OffsetDateTime.parse(executedAt),
                ticker,
                new BigDecimal(quantity),
                "시장가 체결"
        );
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
