package com.woorifisa.won_invest_channel_server.domain.etf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorifisa.won_invest_channel_server.domain.etf.external.CoreEtfProductApi;
import com.woorifisa.won_invest_channel_server.domain.etf.external.KisOverseasProductInfoApi;
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
    private KisOverseasProductInfoApi kisOverseasProductInfoApi;

    @Mock
    private KisEtfProductMapper kisEtfProductMapper;

    @Spy
    private EtfProductEligibilityValidator etfProductEligibilityValidator = new EtfProductEligibilityValidator();
    @Mock
    private CoreEtfProductApi coreEtfProductApi;

    @Mock
    private InvestChnEtfProductCommandService investChnEtfProductCommandService;

    @InjectMocks
    private InvestChnEtfProductSyncService syncService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("ETF 후보별 동기화 결과를 synced, skipped, failed로 집계한다")
    void syncCuratedEtfProducts_countsSyncedSkippedAndFailedItems() {
        // given
        CuratedEtfProductCandidate spyCandidate = candidate("SPY", 1);
        CuratedEtfProductCandidate xlpCandidate = candidate("XLP", 2);
        CuratedEtfProductCandidate qqqCandidate = candidate("QQQ", 3);

        when(candidateProvider.getCandidates())
                .thenReturn(List.of(spyCandidate, xlpCandidate, qqqCandidate));

        KisOverseasProductInfoResponse spyKisResponse = kisResponse("SPY");
        KisOverseasProductInfoResponse xlpKisResponse = kisResponse("XLP");
        KisOverseasProductInfoResponse qqqKisResponse = kisResponse("QQQ");

        when(kisOverseasProductInfoApi.getProductInfo(
                spyCandidate.productTypeCode(),
                spyCandidate.ticker()
        )).thenReturn(spyKisResponse);

        when(kisOverseasProductInfoApi.getProductInfo(
                xlpCandidate.productTypeCode(),
                xlpCandidate.ticker()
        )).thenReturn(xlpKisResponse);

        when(kisOverseasProductInfoApi.getProductInfo(
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

        when(coreEtfProductApi.upsertEtfProduct(any(CoreEtfProductUpsertRequest.class)))
                .thenReturn(coreResponse);

        // when
        EtfProductSyncResult result = syncService.syncCuratedEtfProducts();

        // then
        assertThat(result.totalCount()).isEqualTo(3);
        assertThat(result.syncedCount()).isEqualTo(2);
        assertThat(result.skippedCount()).isEqualTo(1);
        assertThat(result.failedCount()).isEqualTo(0);

        assertThat(result.skippedItems())
                .hasSize(1)
                .anySatisfy(item -> {
                    assertThat(item.ticker()).isEqualTo("XLP");
                    assertThat(item.reason()).contains("소수점 매수 가능 상품이 아닙니다.");
                });

        assertThat(result.failedItems()).isEmpty();
    }

    @Test
    @DisplayName("Core 동기화 중 예외가 발생하면 failedItems에 일반화된 실패 사유를 추가한다")
    void syncCuratedEtfProducts_whenCoreSyncFails_addsFailedItem() {
        // given
        CuratedEtfProductCandidate spyCandidate = candidate("SPY", 1);

        when(candidateProvider.getCandidates())
                .thenReturn(List.of(spyCandidate));

        KisOverseasProductInfoResponse spyKisResponse = kisResponse("SPY");

        when(kisOverseasProductInfoApi.getProductInfo(
                spyCandidate.productTypeCode(),
                spyCandidate.ticker()
        )).thenReturn(spyKisResponse);

        ExternalEtfProduct spyProduct = product("SPY", true, true, true);

        when(kisEtfProductMapper.toExternalEtfProduct(spyCandidate, spyKisResponse))
                .thenReturn(spyProduct);

        when(coreEtfProductApi.upsertEtfProduct(any(CoreEtfProductUpsertRequest.class)))
                .thenThrow(new IllegalStateException("Core ETF 상품 동기화 API 호출 실패"));

        // when
        EtfProductSyncResult result = syncService.syncCuratedEtfProducts();

        // then
        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.syncedCount()).isEqualTo(0);
        assertThat(result.skippedCount()).isEqualTo(0);
        assertThat(result.failedCount()).isEqualTo(1);

        assertThat(result.failedItems())
                .hasSize(1)
                .anySatisfy(item -> {
                    assertThat(item.ticker()).isEqualTo("SPY");
                    assertThat(item.reason()).isEqualTo("동기화 처리 중 오류가 발생했습니다.");
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

    private KisOverseasProductInfoResponse kisResponse(String ticker) {
        try {
            String json = """
                {
                  "rt_cd": "0",
                  "msg_cd": "MCA00000",
                  "msg1": "정상처리 되었습니다.",
                  "output": {
                    "std_pdno": "US-%s",
                    "istt_usge_isin_cd": "US-%s",
                    "prdt_name": "%s ETF",
                    "prdt_eng_name": "%s ETF",
                    "tr_crcy_cd": "USD",
                    "ovrs_stck_etf_risk_drtp_cd": "001",
                    "lstg_yn": "Y",
                    "lstg_abol_item_yn": "N",
                    "ovrs_stck_tr_stop_dvsn_cd": "01",
                    "mint_svc_yn": "Y",
                    "mini_stk_tr_stat_dvsn_cd": "01",
                    "mint_dcpt_trad_psbl_yn": "Y"
                  }
                }
                """.formatted(
                    ticker,
                    ticker,
                    ticker,
                    ticker
            );

            return objectMapper.readValue(json, KisOverseasProductInfoResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("테스트용 KIS 응답 생성 실패", e);
        }
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
