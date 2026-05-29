package com.woorifisa.won_invest_channel_server.domain.etf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.woorifisa.won_invest_channel_server.domain.etf.client.CoreEtfProductClient;
import com.woorifisa.won_invest_channel_server.domain.etf.client.KisOverseasProductInfoClient;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.core.request.CoreEtfProductUpsertRequest;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.core.response.CoreEtfProductUpsertResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.kis.response.KisOverseasProductInfoResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.CuratedEtfProductCandidate;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.EtfProductSyncResult;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.ExternalEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.mapper.KisEtfProductMapper;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfProductStatus;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfRiskGrade;
import com.woorifisa.won_invest_channel_server.domain.etf.provider.CuratedEtfProductCandidateProvider;
import com.woorifisa.won_invest_channel_server.domain.etf.validator.EtfProductEligibilityValidator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvestChnEtfProductSyncServiceTest {

    @Mock
    private CuratedEtfProductCandidateProvider candidateProvider;

    @Mock
    private KisOverseasProductInfoClient kisOverseasProductInfoClient;

    @Mock
    private KisEtfProductMapper kisEtfProductMapper;

    @Spy
    private EtfProductEligibilityValidator etfProductEligibilityValidator = new EtfProductEligibilityValidator();
    @Mock
    private CoreEtfProductClient coreEtfProductClient;

    @Mock
    private InvestChnEtfProductCommandService investChnEtfProductCommandService;

    @InjectMocks
    private InvestChnEtfProductService syncService;

    @Test
    @DisplayName("ETF 후보별 동기화 결과를 synced, skipped, failed로 집계한다")
    void syncCuratedEtfProducts_countsSyncedSkippedAndFailedItems() {
        // given
        CuratedEtfProductCandidate spyCandidate = candidate("SPY", 1);
        CuratedEtfProductCandidate xlpCandidate = candidate("XLP", 2);
        CuratedEtfProductCandidate qqqCandidate = candidate("QQQ", 3);

        when(candidateProvider.getCandidates())
                .thenReturn(List.of(spyCandidate, xlpCandidate, qqqCandidate));

        KisOverseasProductInfoResponse spyKisResponse = mock(KisOverseasProductInfoResponse.class);
        KisOverseasProductInfoResponse xlpKisResponse = mock(KisOverseasProductInfoResponse.class);
        KisOverseasProductInfoResponse qqqKisResponse = mock(KisOverseasProductInfoResponse.class);

        /*
         * 여기서 getProductInfo 메서드명이 다르면,
         * 실제 KisOverseasProductInfoClient의 public 메서드명으로 바꿔줘.
         */
        when(kisOverseasProductInfoClient.getProductInfo(
                spyCandidate.productTypeCode(),
                spyCandidate.ticker()
        )).thenReturn(spyKisResponse);

        when(kisOverseasProductInfoClient.getProductInfo(
                xlpCandidate.productTypeCode(),
                xlpCandidate.ticker()
        )).thenReturn(xlpKisResponse);

        when(kisOverseasProductInfoClient.getProductInfo(
                qqqCandidate.productTypeCode(),
                qqqCandidate.ticker()
        )).thenReturn(qqqKisResponse);

        ExternalEtfProduct spyProduct = product("SPY", true, true, true);
        ExternalEtfProduct xlpProduct = product("XLP", true, false, true);
        ExternalEtfProduct qqqProduct = product("QQQ", true, true, true);

        when(kisEtfProductMapper.toExternalEtfProduct(spyCandidate, spyKisResponse))
                .thenReturn(spyProduct);
        when(kisEtfProductMapper.toExternalEtfProduct(xlpCandidate, xlpKisResponse))
                .thenReturn(xlpProduct);
        when(kisEtfProductMapper.toExternalEtfProduct(qqqCandidate, qqqKisResponse))
                .thenReturn(qqqProduct);

        CoreEtfProductUpsertResponse coreResponse = mock(CoreEtfProductUpsertResponse.class);
        when(coreResponse.etfId()).thenReturn(1L);

        when(coreEtfProductClient.upsertEtfProduct(any(CoreEtfProductUpsertRequest.class)))
                .thenReturn(coreResponse)
                .thenThrow(new IllegalStateException("Core ETF 상품 동기화 API 호출 실패. status=500 INTERNAL_SERVER_ERROR"));

        // when
        EtfProductSyncResult result = syncService.syncCuratedEtfProducts();

        // then
        assertThat(result.totalCount()).isEqualTo(3);
        assertThat(result.syncedCount()).isEqualTo(1);
        assertThat(result.skippedCount()).isEqualTo(1);
        assertThat(result.failedCount()).isEqualTo(1);

        assertThat(result.skippedItems())
                .hasSize(1)
                .anySatisfy(item -> {
                    assertThat(item.ticker()).isEqualTo("XLP");
                    assertThat(item.reason()).contains("소수점 매수 가능 상품이 아닙니다.");
                });

        assertThat(result.failedItems())
                .hasSize(1)
                .anySatisfy(item -> {
                    assertThat(item.ticker()).isEqualTo("QQQ");
                    assertThat(item.reason()).contains("동기화 처리 중 오류가 발생했습니다.");
                });
    }

    private CuratedEtfProductCandidate candidate(String ticker, int displayOrder) {
        return CuratedEtfProductCandidate.kis(
                "529",
                ticker,
                ticker + " ETF 후보 상품",
                EtfRiskGrade.MEDIUM,
                displayOrder
        );
    }

    private ExternalEtfProduct product(
            String ticker,
            boolean isEtf,
            boolean isFractionalAvailable,
            boolean isTradeAvailable
    ) {
        return new ExternalEtfProduct(
                "KIS",
                "US-" + ticker,
                ticker,
                "US-" + ticker,
                ticker + " ETF",
                ticker + " ETF 후보 상품",
                "AMEX",
                EtfCurrency.USD,
                isTradeAvailable ? EtfProductStatus.ACTIVE : EtfProductStatus.INACTIVE,
                EtfRiskGrade.MEDIUM,
                isEtf,
                isFractionalAvailable,
                isTradeAvailable,
                1
        );
    }
}