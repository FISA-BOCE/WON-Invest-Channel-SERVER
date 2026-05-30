package com.woorifisa.won_invest_channel_server.domain.etf.service;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.EtfProductSummaryResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.EtfProductListResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.model.InvestChnEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfRiskGrade;
import com.woorifisa.won_invest_channel_server.domain.etf.repository.InvestChnEtfProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class InvestChnEtfProductQueryServiceTest {

    @Mock
    private InvestChnEtfProductRepository repository;

    @InjectMocks
    private InvestChnEtfProductQueryService service;

    @Test
    @DisplayName("제공 가능한 USD/거래가능/소수점가능 ETF만 반환")
    void getProvidedEtfProducts_returnsRepositoryResultsAsResponse() {
        // given
        when(repository.findProvidedEtfProducts(null, null, null, null))
                .thenReturn(List.of(
                        product(1L, "VOO", EtfCurrency.USD, true, true, 1)
                ));

        // when
        EtfProductListResponse response =
                service.getProvidedEtfProducts(null, null, null, null);

        // then
        assertThat(response.etfs())
                .extracting(EtfProductSummaryResponse::ticker)
                .containsExactly("VOO");
    }

    @Test
    @DisplayName("검색 조건의 공백 문자열을 trim 후 Repository에 전달한다")
    void getProvidedEtfProducts_trimsSearchConditions() {
        // given
        when(repository.findProvidedEtfProducts(
                "VOO",
                "NYSE",
                EtfCurrency.USD,
                EtfRiskGrade.MEDIUM
        )).thenReturn(List.of(
                product(1L, "VOO", EtfCurrency.USD, true, true, 1)
        ));

        // when
        EtfProductListResponse response =
                service.getProvidedEtfProducts(
                        " VOO ",
                        " NYSE ",
                        EtfCurrency.USD,
                        EtfRiskGrade.MEDIUM
                );

        // then
        assertThat(response.etfs())
                .extracting(EtfProductSummaryResponse::ticker)
                .containsExactly("VOO");
    }

    private InvestChnEtfProduct product(
            Long etfId,
            String ticker,
            EtfCurrency currency,
            boolean isTradeAvailable,
            boolean isFractionalAvailable,
            Integer displayOrder
    ) {
        return InvestChnEtfProduct.create(
                etfId,
                "KIS",
                "US-" + ticker,
                ticker,
                ticker + " ETF",
                ticker + " ETF 설명",
                "NYSE",
                currency,
                EtfRiskGrade.MEDIUM,
                isFractionalAvailable,
                isTradeAvailable,
                displayOrder,
                LocalDateTime.now()
        );
    }
}