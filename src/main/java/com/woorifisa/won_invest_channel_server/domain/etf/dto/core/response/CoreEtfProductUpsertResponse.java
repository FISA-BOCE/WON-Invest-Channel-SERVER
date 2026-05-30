package com.woorifisa.won_invest_channel_server.domain.etf.dto.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfProductStatus;

// Core 서버 ETF 원천 상품 동기화 API - data 응답 DTO
@JsonIgnoreProperties(ignoreUnknown = true)
public record CoreEtfProductUpsertResponse(

        Long etfId,

        String externalProvider,

        String externalEtfId,

        String ticker,

        String isin,

        String etfName,

        String market,

        EtfCurrency currency,

        EtfProductStatus productStatus,

        @JsonProperty("isFractionalAvailable")
        boolean isFractionalAvailable,

        @JsonProperty("isTradeAvailable")
        boolean isTradeAvailable
) {
}
