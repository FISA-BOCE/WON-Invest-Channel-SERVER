package com.woorifisa.won_invest_channel_server.domain.etf.dto.response;

import com.woorifisa.won_invest_channel_server.domain.etf.model.InvestChnEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfRiskGrade;
import java.time.LocalDateTime;
import java.util.List;

public record InvestEtfProductListResponse(
        List<EtfSummary> etfs
) {

    public record EtfSummary(
            Long etfId,
            String ticker,
            String etfName,
            String description,
            String market,
            EtfCurrency currency,
            EtfRiskGrade riskGrade,
            boolean isTradeAvailable,
            boolean isFractionalAvailable,
            boolean isAutoInvestAvailable,
            Integer displayOrder,
            LocalDateTime lastSyncedAt
    ) {

        public static EtfSummary from(InvestChnEtfProduct product) {
            return new EtfSummary(
                    product.getEtfId(),
                    product.getTicker(),
                    product.getEtfName(),
                    product.getDescription(),
                    product.getMarket(),
                    product.getCurrency(),
                    product.getRiskGrade(),
                    Boolean.TRUE.equals(product.getIsTradeAvailable()),
                    Boolean.TRUE.equals(product.getIsFractionalAvailable()),
                    true,
                    product.getDisplayOrder(),
                    product.getLastSyncedAt()
            );
        }
    }
}
