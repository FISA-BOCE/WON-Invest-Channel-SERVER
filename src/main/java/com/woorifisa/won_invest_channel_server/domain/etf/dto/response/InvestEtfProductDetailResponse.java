package com.woorifisa.won_invest_channel_server.domain.etf.dto.response;

import com.woorifisa.won_invest_channel_server.domain.etf.model.InvestChnEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfRiskGrade;

public record InvestEtfProductDetailResponse(
        Long etfId,
        String etfName,
        String ticker,
        String market,
        EtfCurrency currency,
        EtfRiskGrade riskGrade
) {

    public static InvestEtfProductDetailResponse from(InvestChnEtfProduct product) {
        return new InvestEtfProductDetailResponse(
                product.getEtfId(),
                product.getEtfName(),
                product.getTicker(),
                product.getMarket(),
                product.getCurrency(),
                product.getRiskGrade()
        );
    }
}
