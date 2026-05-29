package com.woorifisa.won_invest_channel_server.domain.etf.dto.core.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.ExternalEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfProductStatus;
import com.woorifisa.won_invest_channel_server.domain.etf.exception.code.EtfErrorCode;
import com.woorifisa.won_invest_channel_server.domain.etf.exception.EtfSyncException;

// Channel 서버가 Core 서버의 ETF 원천 상품 동기화 API로 보내는 요청 DTO
// description, riskGrade, displayOrder - Core 요청에 보내지 않음 (화면 표시용(관리자가 입력)이라 채널에만 저장)

public record CoreEtfProductUpsertRequest(

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

    public static CoreEtfProductUpsertRequest from(ExternalEtfProduct product) {
        if (product == null) {
            throw new EtfSyncException(EtfErrorCode.ETF_PRODUCT_EMPTY);
        }

        return new CoreEtfProductUpsertRequest(
                product.externalProvider(),
                product.externalEtfId(),
                product.ticker(),
                product.isin(),
                product.etfName(),
                product.market(),
                product.currency(),
                product.productStatus(),
                product.isFractionalAvailable(),
                product.isTradeAvailable()
        );
    }
}
