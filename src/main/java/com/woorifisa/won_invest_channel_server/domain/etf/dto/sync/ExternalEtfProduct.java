package com.woorifisa.won_invest_channel_server.domain.etf.dto.sync;

import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfProductStatus;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfRiskGrade;

/**
 * 외부 ETF 정보를 Invest-Channel 서버 내부에서 사용하기 위해 정규화한 DTO - CuratedEtfProductCandidate와 KIS 상품기본정보 응답을 합쳐 만듦
 */
public record ExternalEtfProduct(

        // 외부 제공자 - ex) KIS
        String externalProvider,

        // 외부 ETF 식별자 - KIS 기준으로는 std_pdno 또는 ticker를 사용
        String externalEtfId,

        // ETF 티커 - ex) SPY, QQQ, VOO
        String ticker,

        // ISIN 코드 - Core 원천 ETF 상품 정보에 저장할 때 사용
        String isin,

        // ETF 상품명
        String etfName,

        // 화면 표시용 설명
        String description,

        // 거래 시장 또는 거래소 코드 - ex) NASD, NYSE, AMEX
        String market,

        // 거래 통화
        EtfCurrency currency,

        // 상품 상태 - ex) ACTIVE, INACTIVE
        EtfProductStatus productStatus,

        // 화면 표시용 위험 등급
        EtfRiskGrade riskGrade,

        boolean isEtf,

        // 소수점 매수 가능 여부
        boolean isFractionalAvailable,

        // 거래 가능 여부
        boolean isTradeAvailable,


        // 화면 노출 순서
        Integer displayOrder
) {

    public boolean isActive() {
        return productStatus == EtfProductStatus.ACTIVE;
    }

    public boolean isUsd() {
        return currency == EtfCurrency.USD;
    }

    public boolean isEtf() {
        return Boolean.TRUE.equals(isEtf);
    }

    public boolean isFractionalAvailable() {
        return Boolean.TRUE.equals(isFractionalAvailable);
    }

    public boolean isTradeAvailable() {
        return Boolean.TRUE.equals(isTradeAvailable);
    }
}


