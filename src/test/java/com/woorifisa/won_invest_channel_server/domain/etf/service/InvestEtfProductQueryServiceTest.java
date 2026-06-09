package com.woorifisa.won_invest_channel_server.domain.etf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.InvestEtfProductDetailResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.InternalInvestEtfDetailResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.exception.EtfSyncException;
import com.woorifisa.won_invest_channel_server.domain.etf.exception.code.EtfErrorCode;
import com.woorifisa.won_invest_channel_server.domain.etf.model.InvestChnEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfRiskGrade;
import com.woorifisa.won_invest_channel_server.domain.etf.repository.InvestChnEtfProductRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvestEtfProductQueryServiceTest {

    @Mock
    private InvestChnEtfProductRepository investChnEtfProductRepository;

    @InjectMocks
    private InvestEtfProductQueryService investEtfProductQueryService;

    @Test
    @DisplayName("ETF 상품 상세 조회 시 필요한 필드를 응답으로 반환한다")
    void getEtfProductDetail_success() {
        InvestChnEtfProduct product = InvestChnEtfProduct.create(
                1L,
                "KIS",
                "US-VOO",
                "VOO",
                "Vanguard S&P 500 ETF",
                "미국 대표 대형주 ETF",
                "AMEX",
                EtfCurrency.USD,
                EtfRiskGrade.MEDIUM,
                true,
                true,
                1,
                LocalDateTime.of(2026, 6, 4, 12, 0)
        );

        given(investChnEtfProductRepository.findById(1L)).willReturn(Optional.of(product));

        InvestEtfProductDetailResponse response = investEtfProductQueryService.getEtfProductDetail(1L);

        assertThat(response.etfId()).isEqualTo(1L);
        assertThat(response.etfName()).isEqualTo("Vanguard S&P 500 ETF");
        assertThat(response.ticker()).isEqualTo("VOO");
        assertThat(response.market()).isEqualTo("AMEX");
        assertThat(response.currency()).isEqualTo(EtfCurrency.USD);
        assertThat(response.riskGrade()).isEqualTo(EtfRiskGrade.MEDIUM);
    }

    @Test
    @DisplayName("ETF 상품이 없으면 NOT_FOUND 예외를 반환한다")
    void getEtfProductDetail_notFound() {
        given(investChnEtfProductRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> investEtfProductQueryService.getEtfProductDetail(999L))
                .isInstanceOf(EtfSyncException.class)
                .satisfies(ex -> assertThat(((EtfSyncException) ex).getErrorCode())
                        .isEqualTo(EtfErrorCode.ETF_PRODUCT_NOT_FOUND));
    }

    @Test
    @DisplayName("위험등급이 null이면 null 그대로 반환한다")
    void getEtfProductDetail_nullRiskGrade_returnsNull() {
        InvestChnEtfProduct product = InvestChnEtfProduct.create(
                2L,
                "KIS",
                "US-SPY",
                "SPY",
                "SPDR S&P 500 ETF Trust",
                "미국 대표 지수 ETF",
                "NYSE",
                EtfCurrency.USD,
                null,
                true,
                true,
                2,
                LocalDateTime.of(2026, 6, 4, 12, 0)
        );

        given(investChnEtfProductRepository.findById(2L)).willReturn(Optional.of(product));

        InvestEtfProductDetailResponse response = investEtfProductQueryService.getEtfProductDetail(2L);

        assertThat(response.riskGrade()).isNull();
    }

    @Test
    @DisplayName("내부 ETF 단건 조회 시 카드 채널 검증용 필드를 반환한다")
    void getInternalEtfProductDetail_success() {
        InvestChnEtfProduct product = InvestChnEtfProduct.create(
                3L,
                "KIS",
                "US-QQQ",
                "QQQ",
                "Invesco QQQ Trust",
                "기술주 중심 ETF",
                "NASDAQ",
                EtfCurrency.USD,
                EtfRiskGrade.MEDIUM,
                false,
                true,
                3,
                LocalDateTime.of(2026, 6, 4, 12, 0)
        );

        given(investChnEtfProductRepository.findById(3L)).willReturn(Optional.of(product));

        InternalInvestEtfDetailResponse response = investEtfProductQueryService.getInternalEtfProductDetail(3L);

        assertThat(response.etfId()).isEqualTo(3L);
        assertThat(response.etfName()).isEqualTo("Invesco QQQ Trust");
        assertThat(response.ticker()).isEqualTo("QQQ");
        assertThat(response.isTradeAvailable()).isTrue();
        assertThat(response.isFractionalAvailable()).isFalse();
    }
}
