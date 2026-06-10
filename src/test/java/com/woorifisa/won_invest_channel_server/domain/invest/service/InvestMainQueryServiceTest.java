package com.woorifisa.won_invest_channel_server.domain.invest.service;

import com.woorifisa.won_invest_channel_server.domain.account.model.AccountStatus;
import com.woorifisa.won_invest_channel_server.domain.account.model.InvestChnAccountSummary;
import com.woorifisa.won_invest_channel_server.domain.account.repository.InvestChnAccountSummaryRepository;
import com.woorifisa.won_invest_channel_server.domain.aidb.model.InvestChnAiSummary;
import com.woorifisa.won_invest_channel_server.domain.aidb.repository.InvestChnAiSummaryRepository;
import com.woorifisa.won_invest_channel_server.domain.holding.model.InvestChnEtfHoldingSummary;
import com.woorifisa.won_invest_channel_server.domain.holding.repository.InvestChnEtfHoldingSummaryRepository;
import com.woorifisa.won_invest_channel_server.domain.invest.dto.response.InvestMainResponse;
import com.woorifisa.won_invest_channel_server.domain.invest.exception.code.InvestErrorCode;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class InvestMainQueryServiceTest {

    private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ACCOUNT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private InvestChnAccountSummaryRepository accountSummaryRepository;

    @Mock
    private InvestChnAiSummaryRepository aiSummaryRepository;

    @Mock
    private InvestChnEtfHoldingSummaryRepository holdingSummaryRepository;

    @InjectMocks
    private InvestMainQueryService investMainQueryService;

    @Test
    @DisplayName("ACTIVE 계좌가 있으면 가장 최근 ACTIVE 계좌 기준으로 메인 화면 응답을 반환한다")
    void getInvestMain_successWithActiveAccount() {
        given(accountSummaryRepository.findAllByUserUuidAndAccountStatusOrderByCreatedAtDesc(USER_UUID, AccountStatus.ACTIVE))
                .willReturn(List.of(account(AccountStatus.ACTIVE, ACCOUNT_UUID, "123-***-***456")));
        given(aiSummaryRepository.findTopByUserUuidAndInvestAccountUuidOrderByLastSyncedAtDescSummaryIdDesc(USER_UUID, ACCOUNT_UUID))
                .willReturn(Optional.of(aiSummary()));
        given(holdingSummaryRepository.findTop2ByInvestAccountUuidOrderByLastSyncedAtDescHoldingSummaryIdDesc(ACCOUNT_UUID))
                .willReturn(List.of(
                        holding("QQQ", "나스닥 100 ETF", "0.0241", "16280"),
                        holding("VOO", "S&P 500 ETF", "0.0132", "11800")
                ));

        InvestMainResponse response = investMainQueryService.getInvestMain(USER_UUID);

        assertThat(response.totalEvaluationAmount()).isEqualByComparingTo("79420.00");
        assertThat(response.profitLossAmount()).isEqualByComparingTo("4820.00");
        assertThat(response.profitLossRate()).isEqualByComparingTo("6.45");
        assertThat(response.account().investAccountUuid()).isEqualTo(ACCOUNT_UUID);
        assertThat(response.account().accountHolderName()).isEqualTo("홍*동");
        assertThat(response.cashBalance().krwAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.cashBalance().krwStatus()).isEqualTo("전액 환전·매수 완료");
        assertThat(response.recentPayments()).hasSize(2);
        assertThat(response.recentPayments().get(0).ticker()).isEqualTo("QQQ");
        assertThat(response.recentPayments().get(1).ticker()).isEqualTo("VOO");
    }

    @Test
    @DisplayName("ACTIVE 계좌가 없으면 가장 최근 계좌를 대표 계좌로 사용한다")
    void getInvestMain_fallbackToLatestAccount() {
        given(accountSummaryRepository.findAllByUserUuidAndAccountStatusOrderByCreatedAtDesc(USER_UUID, AccountStatus.ACTIVE))
                .willReturn(List.of());
        given(accountSummaryRepository.findAllByUserUuidOrderByCreatedAtDesc(USER_UUID))
                .willReturn(List.of(account(AccountStatus.SUSPENDED, ACCOUNT_UUID, "123-***-***456")));
        given(aiSummaryRepository.findTopByUserUuidAndInvestAccountUuidOrderByLastSyncedAtDescSummaryIdDesc(USER_UUID, ACCOUNT_UUID))
                .willReturn(Optional.empty());
        given(holdingSummaryRepository.findTop2ByInvestAccountUuidOrderByLastSyncedAtDescHoldingSummaryIdDesc(ACCOUNT_UUID))
                .willReturn(List.of());

        InvestMainResponse response = investMainQueryService.getInvestMain(USER_UUID);

        assertThat(response.totalEvaluationAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.profitLossAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.profitLossRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.recentPayments()).isEmpty();
    }

    @Test
    @DisplayName("대표 계좌가 없으면 ACCOUNT_NOT_FOUND 예외가 발생한다")
    void getInvestMain_accountNotFound() {
        given(accountSummaryRepository.findAllByUserUuidAndAccountStatusOrderByCreatedAtDesc(USER_UUID, AccountStatus.ACTIVE))
                .willReturn(List.of());
        given(accountSummaryRepository.findAllByUserUuidOrderByCreatedAtDesc(USER_UUID))
                .willReturn(List.of());

        assertThatThrownBy(() -> investMainQueryService.getInvestMain(USER_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestErrorCode.ACCOUNT_NOT_FOUND));

        verifyNoInteractions(aiSummaryRepository);
    }

    @Test
    @DisplayName("예금주명이 세 글자면 가운데 한 글자를 마스킹한다")
    void getInvestMain_masksThreeCharacterName() {
        assertThat(InvestMainQueryService.maskAccountHolderName("김우리")).isEqualTo("김*리");
    }

    @Test
    @DisplayName("예금주명이 네 글자면 가운데 두 글자를 마스킹한다")
    void getInvestMain_masksFourCharacterName() {
        assertThat(InvestMainQueryService.maskAccountHolderName("남궁종연")).isEqualTo("남**연");
    }

    private InvestChnAccountSummary account(AccountStatus accountStatus, UUID accountUuid, String accountNoDisplay) {
        return InvestChnAccountSummary.builder()
                .investAccountUuid(accountUuid)
                .investUserUuid(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                .userUuid(USER_UUID)
                .accountNoDisplay(accountNoDisplay)
                .accountHolderName("홍길동")
                .accountStatus(accountStatus)
                .build();
    }

    private InvestChnAiSummary aiSummary() {
        return InvestChnAiSummary.builder()
                .userUuid(USER_UUID)
                .investAccountUuid(ACCOUNT_UUID)
                .totalBuyAmount(new BigDecimal("74600.00"))
                .evaluationAmount(new BigDecimal("79420.00"))
                .profitLossAmount(new BigDecimal("4820.00"))
                .profitLossRate(new BigDecimal("6.45"))
                .lastSyncedAt(LocalDateTime.of(2026, 6, 9, 9, 0))
                .build();
    }

    private InvestChnEtfHoldingSummary holding(String ticker, String etfName, String quantity, String evaluationAmount) {
        return InvestChnEtfHoldingSummary.builder()
                .userUuid(USER_UUID)
                .investUserUuid(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                .investAccountUuid(ACCOUNT_UUID)
                .etfId(1L)
                .ticker(ticker)
                .etfName(etfName)
                .holdingQuantity(new BigDecimal(quantity))
                .averageBuyPrice(new BigDecimal("100.00"))
                .evaluationAmount(new BigDecimal(evaluationAmount))
                .profitLossAmount(new BigDecimal("10.00"))
                .profitLossRate(new BigDecimal("1.00"))
                .lastSyncedAt(LocalDateTime.of(2026, 6, 9, 9, 0))
                .build();
    }
}
