package com.woorifisa.won_invest_channel_server.domain.invest.service;

import com.woorifisa.won_invest_channel_server.domain.account.model.AccountStatus;
import com.woorifisa.won_invest_channel_server.domain.account.model.InvestChnAccountSummary;
import com.woorifisa.won_invest_channel_server.domain.account.repository.InvestChnAccountSummaryRepository;
import com.woorifisa.won_invest_channel_server.domain.invest.dto.response.InvestEtfHoldingsResponse;
import com.woorifisa.won_invest_channel_server.domain.invest.exception.code.InvestErrorCode;
import com.woorifisa.won_invest_channel_server.domain.invest.external.InvestCoreEtfQueryApi;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import feign.FeignException;
import feign.Request;
import feign.Response;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private InvestCoreEtfQueryApi investCoreEtfQueryApi;

    @InjectMocks
    private InvestEtfQueryService investEtfQueryService;

    @Test
    @DisplayName("정상 조회 시 총 평가금액, 손익, 수익률, 최근 매수 3건을 반환한다")
    void getAccountEtfs_success() {
        InvestChnAccountSummary account = account(AccountStatus.ACTIVE, USER_UUID);
        InvestEtfHoldingsResponse coreData = response();
        given(accountSummaryRepository.findById(ACCOUNT_UUID)).willReturn(Optional.of(account));
        given(investCoreEtfQueryApi.getAccountEtfHoldings(USER_UUID, ACCOUNT_UUID))
                .willReturn(new ApiResponse<>(200, "INVEST_200_005", "보유 ETF 조회가 완료되었습니다.", coreData));

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
        then(investCoreEtfQueryApi).should(never()).getAccountEtfHoldings(USER_UUID, ACCOUNT_UUID);
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
        then(investCoreEtfQueryApi).should(never()).getAccountEtfHoldings(USER_UUID, ACCOUNT_UUID);
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
        then(investCoreEtfQueryApi).should(never()).getAccountEtfHoldings(USER_UUID, ACCOUNT_UUID);
    }

    @Test
    @DisplayName("Core 응답 data가 없으면 INTERNAL_QUERY_FAILED 예외가 발생한다")
    void getAccountEtfs_coreResponseWithoutData() {
        given(accountSummaryRepository.findById(ACCOUNT_UUID))
                .willReturn(Optional.of(account(AccountStatus.ACTIVE, USER_UUID)));
        given(investCoreEtfQueryApi.getAccountEtfHoldings(USER_UUID, ACCOUNT_UUID))
                .willReturn(new ApiResponse<>(200, "INVEST_200_005", "보유 ETF 조회가 완료되었습니다.", null));

        assertThatThrownBy(() -> investEtfQueryService.getAccountEtfs(USER_UUID, ACCOUNT_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestErrorCode.INTERNAL_QUERY_FAILED));
    }

    @Test
    @DisplayName("Core 404 응답이면 ACCOUNT_NOT_FOUND 예외가 발생한다")
    void getAccountEtfs_coreNotFound() {
        given(accountSummaryRepository.findById(ACCOUNT_UUID))
                .willReturn(Optional.of(account(AccountStatus.ACTIVE, USER_UUID)));
        given(investCoreEtfQueryApi.getAccountEtfHoldings(USER_UUID, ACCOUNT_UUID))
                .willThrow(feignException(404));

        assertThatThrownBy(() -> investEtfQueryService.getAccountEtfs(USER_UUID, ACCOUNT_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestErrorCode.ACCOUNT_NOT_FOUND));
    }

    @Test
    @DisplayName("Core 403 응답이면 ACCOUNT_NOT_OWNER 예외가 발생한다")
    void getAccountEtfs_coreForbidden() {
        given(accountSummaryRepository.findById(ACCOUNT_UUID))
                .willReturn(Optional.of(account(AccountStatus.ACTIVE, USER_UUID)));
        given(investCoreEtfQueryApi.getAccountEtfHoldings(USER_UUID, ACCOUNT_UUID))
                .willThrow(feignException(403));

        assertThatThrownBy(() -> investEtfQueryService.getAccountEtfs(USER_UUID, ACCOUNT_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestErrorCode.ACCOUNT_NOT_OWNER));
    }

    @Test
    @DisplayName("Core 400 응답이면 INVALID_ACCOUNT_STATUS 예외가 발생한다")
    void getAccountEtfs_coreBadRequest() {
        given(accountSummaryRepository.findById(ACCOUNT_UUID))
                .willReturn(Optional.of(account(AccountStatus.ACTIVE, USER_UUID)));
        given(investCoreEtfQueryApi.getAccountEtfHoldings(USER_UUID, ACCOUNT_UUID))
                .willThrow(feignException(400));

        assertThatThrownBy(() -> investEtfQueryService.getAccountEtfs(USER_UUID, ACCOUNT_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestErrorCode.INVALID_ACCOUNT_STATUS));
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
                        execution("2026-05-16T00:00:00", "VOO", "0.0235"),
                        execution("2026-05-15T00:00:00", "QQQ", "0.0100"),
                        execution("2026-05-14T00:00:00", "VOO", "0.0050")
                )
        );
    }

    private InvestEtfHoldingsResponse.RecentExecution execution(String executedAt, String ticker, String quantity) {
        return new InvestEtfHoldingsResponse.RecentExecution(
                LocalDateTime.parse(executedAt),
                ticker,
                new BigDecimal(quantity),
                "시장가 체결"
        );
    }

    private FeignException feignException(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/internal/invest/accounts/" + ACCOUNT_UUID + "/etfs",
                Map.of(),
                null,
                null,
                null
        );
        return FeignException.errorStatus(
                "InvestCoreEtfQueryApi#getAccountEtfHoldings",
                Response.builder()
                        .status(status)
                        .reason("error")
                        .request(request)
                        .build()
        );
    }
}
