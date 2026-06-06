package com.woorifisa.won_invest_channel_server.domain.aidb.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AiDbHoldingSummaryResponse(
        UUID userUuid,
        UUID investAccountUuid,
        BigDecimal totalBuyAmount,
        BigDecimal totalEvaluationAmount,
        BigDecimal totalProfitLossAmount,
        BigDecimal totalProfitLossRate,
        Integer holdingCount,
        LocalDateTime lastSyncedAt,
        List<Holding> holdings
) {

    public record Holding(
            Long etfId,
            String ticker,
            String etfName,
            BigDecimal holdingQuantity,
            BigDecimal averageBuyPrice,
            BigDecimal evaluationAmount,
            BigDecimal profitLossAmount,
            BigDecimal profitLossRate,
            LocalDateTime lastSyncedAt
    ) {
    }
}
