package com.woorifisa.won_invest_channel_server.domain.invest.service;

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

@ExtendWith(MockitoExtension.class)
class InvestCoreEtfQueryClientTest {

    private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ACCOUNT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private InvestCoreEtfQueryApi investCoreEtfQueryApi;

    @InjectMocks
    private InvestCoreEtfQueryClient investCoreEtfQueryClient;

    @Test
    @DisplayName("Core 응답 data를 보유 ETF 응답으로 반환한다")
    void fetchCoreEtfHoldings_success() {
        InvestEtfHoldingsResponse coreData = response();
        given(investCoreEtfQueryApi.getAccountEtfHoldings(USER_UUID, ACCOUNT_UUID))
                .willReturn(new ApiResponse<>(200, "INVEST_200_005", "보유 ETF 조회가 완료되었습니다.", coreData));

        InvestEtfHoldingsResponse response = investCoreEtfQueryClient.fetchCoreEtfHoldings(USER_UUID, ACCOUNT_UUID);

        assertThat(response).isSameAs(coreData);
    }

    @Test
    @DisplayName("Core 응답 data가 없으면 INTERNAL_QUERY_FAILED 예외가 발생한다")
    void fetchCoreEtfHoldings_coreResponseWithoutData() {
        given(investCoreEtfQueryApi.getAccountEtfHoldings(USER_UUID, ACCOUNT_UUID))
                .willReturn(new ApiResponse<>(200, "INVEST_200_005", "보유 ETF 조회가 완료되었습니다.", null));

        assertThatThrownBy(() -> investCoreEtfQueryClient.fetchCoreEtfHoldings(USER_UUID, ACCOUNT_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestErrorCode.INTERNAL_QUERY_FAILED));
    }

    @Test
    @DisplayName("Core 404 응답이면 ACCOUNT_NOT_FOUND 예외가 발생한다")
    void fetchCoreEtfHoldings_coreNotFound() {
        given(investCoreEtfQueryApi.getAccountEtfHoldings(USER_UUID, ACCOUNT_UUID))
                .willThrow(feignException(404));

        assertThatThrownBy(() -> investCoreEtfQueryClient.fetchCoreEtfHoldings(USER_UUID, ACCOUNT_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestErrorCode.ACCOUNT_NOT_FOUND));
    }

    @Test
    @DisplayName("Core 403 응답이면 ACCOUNT_NOT_OWNER 예외가 발생한다")
    void fetchCoreEtfHoldings_coreForbidden() {
        given(investCoreEtfQueryApi.getAccountEtfHoldings(USER_UUID, ACCOUNT_UUID))
                .willThrow(feignException(403));

        assertThatThrownBy(() -> investCoreEtfQueryClient.fetchCoreEtfHoldings(USER_UUID, ACCOUNT_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestErrorCode.ACCOUNT_NOT_OWNER));
    }

    @Test
    @DisplayName("Core 400 응답이면 INVALID_ACCOUNT_STATUS 예외가 발생한다")
    void fetchCoreEtfHoldings_coreBadRequest() {
        given(investCoreEtfQueryApi.getAccountEtfHoldings(USER_UUID, ACCOUNT_UUID))
                .willThrow(feignException(400));

        assertThatThrownBy(() -> investCoreEtfQueryClient.fetchCoreEtfHoldings(USER_UUID, ACCOUNT_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestErrorCode.INVALID_ACCOUNT_STATUS));
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
                List.of(new InvestEtfHoldingsResponse.RecentExecution(
                        LocalDateTime.parse("2026-05-16T00:00:00"),
                        "VOO",
                        new BigDecimal("0.0235"),
                        "시장가 체결"
                ))
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
