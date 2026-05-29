package com.woorifisa.won_invest_channel_server.domain.etf.dto.kis.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KisOverseasProductInfoResponse(

        @JsonProperty("rt_cd")
        String rtCd,

        @JsonProperty("msg_cd")
        String msgCd,

        @JsonProperty("msg1")
        String msg1,

        @JsonProperty("output")
        Output output
) {
    public boolean isSuccess() {
    return "0".equals(rtCd);
}

@JsonIgnoreProperties(ignoreUnknown = true)
    public record Output(

            @JsonProperty("std_pdno")
            String stdPdno,

            @JsonProperty("prdt_eng_name")
            String prdtEngName,

            @JsonProperty("natn_cd")
            String natnCd,

            @JsonProperty("natn_name")
            String natnName,

            @JsonProperty("tr_mket_cd")
            String trMketCd,

            @JsonProperty("tr_mket_name")
            String trMketName,

            @JsonProperty("ovrs_excg_cd")
            String ovrsExcgCd,

            @JsonProperty("ovrs_excg_name")
            String ovrsExcgName,

            @JsonProperty("tr_crcy_cd")
            String trCrcyCd,

            @JsonProperty("crcy_name")
            String crcyName,

            @JsonProperty("ovrs_stck_dvsn_cd")
            String ovrsStckDvsnCd,

            @JsonProperty("prdt_clsf_cd")
            String prdtClsfCd,

            @JsonProperty("prdt_clsf_name")
            String prdtClsfName,

            @JsonProperty("lstg_dt")
            String lstgDt,

            @JsonProperty("ovrs_stck_tr_stop_dvsn_cd")
            String ovrsStckTrStopDvsnCd,

            @JsonProperty("lstg_abol_item_yn")
            String lstgAbolItemYn,

            @JsonProperty("lstg_yn")
            String lstgYn,

            @JsonProperty("prdt_type_cd_2")
            String prdtTypeCd2,

            @JsonProperty("ovrs_item_name")
            String ovrsItemName,

            @JsonProperty("sedol_no")
            String sedolNo,

            @JsonProperty("blbg_tckr_text")
            String blbgTckrText,

            @JsonProperty("ovrs_stck_etf_risk_drtp_cd")
            String ovrsStckEtfRiskDrtpCd,

            @JsonProperty("istt_usge_isin_cd")
            String isttUsgeIsinCd,

            @JsonProperty("mint_svc_yn")
            String mintSvcYn,

            @JsonProperty("mint_svc_yn_chng_dt")
            String mintSvcYnChngDt,

            @JsonProperty("prdt_name")
            String prdtName,

            @JsonProperty("lei_cd")
            String leiCd,

            @JsonProperty("ovrs_stck_stop_rson_cd")
            String ovrsStckStopRsonCd,

            @JsonProperty("lstg_abol_dt")
            String lstgAbolDt,

            @JsonProperty("mini_stk_tr_stat_dvsn_cd")
            String miniStkTrStatDvsnCd,

            @JsonProperty("mint_frst_svc_erlm_dt")
            String mintFrstSvcErlmDt,

            @JsonProperty("mint_dcpt_trad_psbl_yn")
            String mintDcptTradPsblYn,

            @JsonProperty("mint_fnum_trad_psbl_yn")
            String mintFnumTradPsblYn,

            @JsonProperty("mint_cblc_cvsn_ipsb_yn")
            String mintCblcCvsnIpsbYn,

            @JsonProperty("ptp_item_yn")
            String ptpItemYn,

            @JsonProperty("ptp_item_trfx_exmt_yn")
            String ptpItemTrfxExmtYn,

            @JsonProperty("ptp_item_trfx_exmt_strt_dt")
            String ptpItemTrfxExmtStrtDt,

            @JsonProperty("ptp_item_trfx_exmt_end_dt")
            String ptpItemTrfxExmtEndDt,

            @JsonProperty("dtm_tr_psbl_yn")
            String dtmTrPsblYn,

            @JsonProperty("sdrf_stop_ecls_yn")
            String sdrfStopEclsYn,

            @JsonProperty("sdrf_stop_ecls_erlm_dt")
            String sdrfStopEclsErlmDt,

            @JsonProperty("memo_text1")
            String memoText1,

            @JsonProperty("ovrs_now_pric1")
            String ovrsNowPric1,

            @JsonProperty("sgle_item_lvrg_etp_yn")
            String sgleItemLvrgEtpYn,

            @JsonProperty("last_rcvg_dtime")
            String lastRcvgDtime
    ) {
    }
}