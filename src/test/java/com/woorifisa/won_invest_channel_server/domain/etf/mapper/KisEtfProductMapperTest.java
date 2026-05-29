package com.woorifisa.won_invest_channel_server.domain.etf.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.kis.response.KisOverseasProductInfoResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.CuratedEtfProductCandidate;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.ExternalEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfProductStatus;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfRiskGrade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KisEtfProductMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final KisEtfProductMapper mapper = new KisEtfProductMapper();

    @Test
    @DisplayName("(1) KIS 응답의 ETF 위험지표 코드가 001 이면 -> ETF 상품으로 변환")
    void toExternalEtfProduct_whenEtfRiskCodeIs001_mapsAsEtfProduct() throws Exception {
        // given
        CuratedEtfProductCandidate candidate = CuratedEtfProductCandidate.kis(
                "529",
                "SPY",
                "S&P 500 지수를 추종하는 대표 미국 시장 ETF",
                EtfRiskGrade.MEDIUM,
                1
        );

        KisOverseasProductInfoResponse response = kisResponse(
                "US78462F1030",
                "SPDR S&P 500",
                "STATE STREET SPDR S&P 500 ETF",
                "01",
                "001",
                "USD",
                "Y",
                "N",
                "01",
                "Y",
                "01",
                "Y"
        );

        // when
        ExternalEtfProduct result = mapper.toExternalEtfProduct(candidate, response);

        // then
        assertThat(result.externalProvider()).isEqualTo("KIS");
        assertThat(result.externalEtfId()).isEqualTo("US78462F1030");
        assertThat(result.ticker()).isEqualTo("SPY");
        assertThat(result.isin()).isEqualTo("US78462F1030");
        assertThat(result.etfName()).isEqualTo("STATE STREET SPDR S&P 500 ETF");
        assertThat(result.market()).isEqualTo("AMEX");
        assertThat(result.currency()).isEqualTo(EtfCurrency.USD);
        assertThat(result.productStatus()).isEqualTo(EtfProductStatus.ACTIVE);
        assertThat(result.riskGrade()).isEqualTo(EtfRiskGrade.MEDIUM);
        assertThat(result.isEtf()).isTrue();
        assertThat(result.isTradeAvailable()).isTrue();
        assertThat(result.isFractionalAvailable()).isTrue();
        assertThat(result.displayOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("(2) 소수점 매수 관련 KIS 필드가 비어 있으면 -> 소수점 매수 불가로 변환한다")
    void toExternalEtfProduct_whenFractionalFieldsAreBlank_mapsAsNotFractionalAvailable() throws Exception {
        // given
        CuratedEtfProductCandidate candidate = CuratedEtfProductCandidate.kis(
                "529",
                "XLP",
                "필수소비재 섹터에 투자하는 방어형 ETF",
                EtfRiskGrade.LOW,
                5
        );

        KisOverseasProductInfoResponse response = kisResponse(
                "US81369Y3080",
                "SPDR CONSUMER STAPLES SELECT SECTOR",
                "STATE STREET CONSUMER STAPLES SELECT SECTOR SPDR ETF",
                "01",
                "001",
                "USD",
                "Y",
                "N",
                "01",
                "",
                "",
                ""
        );

        // when
        ExternalEtfProduct result = mapper.toExternalEtfProduct(candidate, response);

        // then
        assertThat(result.ticker()).isEqualTo("XLP");
        assertThat(result.isEtf()).isTrue();
        assertThat(result.isTradeAvailable()).isTrue();
        assertThat(result.isFractionalAvailable()).isFalse();
        assertThat(result.productStatus()).isEqualTo(EtfProductStatus.ACTIVE);
    }

    @Test
    @DisplayName("(3) 거래 정지 상태이면 -> 상품 상태를 INACTIVE로 변환한다")
    void toExternalEtfProduct_whenTradingStopped_mapsAsInactive() throws Exception {
        // given
        CuratedEtfProductCandidate candidate = CuratedEtfProductCandidate.kis(
                "529",
                "SPY",
                "S&P 500 지수를 추종하는 대표 미국 시장 ETF",
                EtfRiskGrade.MEDIUM,
                1
        );

        KisOverseasProductInfoResponse response = kisResponse(
                "US78462F1030",
                "SPDR S&P 500",
                "STATE STREET SPDR S&P 500 ETF",
                "01",
                "001",
                "USD",
                "Y",
                "N",
                "02",
                "Y",
                "01",
                "Y"
        );

        // when
        ExternalEtfProduct result = mapper.toExternalEtfProduct(candidate, response);

        // then
        assertThat(result.isEtf()).isTrue();
        assertThat(result.isTradeAvailable()).isFalse();
        assertThat(result.productStatus()).isEqualTo(EtfProductStatus.INACTIVE);
    }

    private KisOverseasProductInfoResponse kisResponse(
            String stdPdno,
            String prdtName,
            String prdtEngName,
            String ovrsStckDvsnCd,
            String ovrsStckEtfRiskDrtpCd,
            String trCrcyCd,
            String lstgYn,
            String lstgAbolItemYn,
            String ovrsStckTrStopDvsnCd,
            String mintSvcYn,
            String miniStkTrStatDvsnCd,
            String mintDcptTradPsblYn
    ) throws Exception {
        String json = """
                {
                  "rt_cd": "0",
                  "msg_cd": "MCA00000",
                  "msg1": "정상처리 되었습니다.",
                  "output": {
                    "std_pdno": "%s",
                    "istt_usge_isin_cd": "%s",
                    "prdt_name": "%s",
                    "prdt_eng_name": "%s",
                    "natn_cd": "840",
                    "natn_name": "미국",
                    "tr_mket_cd": "01",
                    "tr_mket_name": "아멕스",
                    "ovrs_excg_cd": "AMEX",
                    "ovrs_excg_name": "아멕스",
                    "tr_crcy_cd": "%s",
                    "ovrs_stck_dvsn_cd": "%s",
                    "prdt_clsf_cd": "101210",
                    "prdt_clsf_name": "해외주식",
                    "ovrs_stck_etf_risk_drtp_cd": "%s",
                    "lstg_yn": "%s",
                    "lstg_abol_item_yn": "%s",
                    "ovrs_stck_tr_stop_dvsn_cd": "%s",
                    "mint_svc_yn": "%s",
                    "mini_stk_tr_stat_dvsn_cd": "%s",
                    "mint_dcpt_trad_psbl_yn": "%s"
                  }
                }
                """.formatted(
                stdPdno,
                stdPdno,
                prdtName,
                prdtEngName,
                trCrcyCd,
                ovrsStckDvsnCd,
                ovrsStckEtfRiskDrtpCd,
                lstgYn,
                lstgAbolItemYn,
                ovrsStckTrStopDvsnCd,
                mintSvcYn,
                miniStkTrStatDvsnCd,
                mintDcptTradPsblYn
        );

        return objectMapper.readValue(json, KisOverseasProductInfoResponse.class);
    }

    @Test
    @DisplayName("KIS 응답에 알 수 없는 필드가 추가되어도 -> 역직렬화에 성공")
    void deserialize_whenKisResponseHasUnknownFields_doesNotFail() throws Exception {
        // given
        ObjectMapper mapper = new ObjectMapper();

        String json = """
            {
              "rt_cd": "0",
              "msg_cd": "MCA00000",
              "msg1": "정상처리 되었습니다.",
              "unknown_top_level_field": "new-field",
              "output": {
                "std_pdno": "US78462F1030",
                "istt_usge_isin_cd": "US78462F1030",
                "prdt_name": "SPDR S&P 500",
                "prdt_eng_name": "STATE STREET SPDR S&P 500 ETF",
                "tr_crcy_cd": "USD",
                "ovrs_stck_etf_risk_drtp_cd": "001",
                "unknown_output_field": "new-output-field"
              }
            }
            """;

        // when
        KisOverseasProductInfoResponse response =
                mapper.readValue(json, KisOverseasProductInfoResponse.class);

        // then
        assertThat(response.rtCd()).isEqualTo("0");
        assertThat(response.output().stdPdno()).isEqualTo("US78462F1030");
        assertThat(response.output().prdtEngName()).contains("ETF");
    }
}