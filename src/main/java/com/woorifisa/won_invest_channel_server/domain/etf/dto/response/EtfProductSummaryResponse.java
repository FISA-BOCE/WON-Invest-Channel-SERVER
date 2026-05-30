package com.woorifisa.won_invest_channel_server.domain.etf.dto.response;

import com.woorifisa.won_invest_channel_server.domain.etf.model.InvestChnEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfRiskGrade;

import java.util.Objects;

public record EtfProductSummaryResponse(
        Long etfId,
        String ticker,
        String etfName,
        String description,
        String market,
        EtfCurrency currency,
        EtfRiskGrade riskGrade,
        Boolean isFractionalAvailable,
        Boolean isTradeAvailable
) {

    public static EtfProductSummaryResponse from(InvestChnEtfProduct product) {
        Objects.requireNonNull(product, "product must not be null");

        return new EtfProductSummaryResponse(
                product.getEtfId(),
                product.getTicker(),
                product.getEtfName(),
                product.getDescription(),
                product.getMarket(),
                product.getCurrency(),
                product.getRiskGrade(),
                product.getIsFractionalAvailable(),
                product.getIsTradeAvailable()
        );
    }
}
