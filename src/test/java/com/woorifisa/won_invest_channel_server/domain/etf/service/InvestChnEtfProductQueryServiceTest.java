package com.woorifisa.won_invest_channel_server.domain.etf.service;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.EtfProductSummaryResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.EtfProductListResponse;
import static org.assertj.core.api.Assertions.assertThat;
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

import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class InvestChnEtfProductQueryServiceTest {

    @Mock
    private InvestChnEtfProductRepository repository;

    @InjectMocks
    private InvestChnEtfProductQueryService service;

    @Test
    @DisplayName("제공 가능한 USD/거래가능/소수점가능 ETF만 반환한다")
    void getProvidedEtfProducts_returnsOnlyProvidedProducts() {
        // given
        when(repository.findAll()).thenReturn(List.of(
                product(1L, "VOO", EtfCurrency.USD, true, true, 1),
                product(2L, "KRW", EtfCurrency.KRW, true, true, 2),
                product(3L, "STOP", EtfCurrency.USD, false, true, 3),
                product(4L, "NOFRAC", EtfCurrency.USD, true, false, 4)
        ));

        // when
        EtfProductListResponse response =
                service.getProvidedEtfProducts(null, null, null, null);

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
