package com.woorifisa.won_invest_channel_server.domain.etf.dto.response;

import com.woorifisa.won_invest_channel_server.domain.etf.model.InvestChnEtfProduct;

public record InternalInvestEtfDetailResponse(
        Long etfId,
        String etfName,
        String ticker,
        Boolean isTradeAvailable,
        Boolean isFractionalAvailable
) {

    public static InternalInvestEtfDetailResponse from(InvestChnEtfProduct product) {
        return new InternalInvestEtfDetailResponse(
                product.getEtfId(),
                product.getEtfName(),
                product.getTicker(),
                product.getIsTradeAvailable(),
                product.getIsFractionalAvailable()
        );
    }
}
