package com.woorifisa.won_invest_channel_server.domain.invest.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record InvestEtfHoldingsResponse(
        BigDecimal totalEvaluationAmount,
        BigDecimal profitLossAmount,
        BigDecimal profitLossRate,
        List<Holding> holdings,
        List<RecentExecution> recentExecutions
) {

    public record Holding(
            Long etfId,
            String etfName,
            String ticker,
            BigDecimal holdingQuantity,
            BigDecimal averageBuyPrice,
            BigDecimal evaluationAmount,
            BigDecimal profitLossAmount,
            BigDecimal profitLossRate
    ) {
    }

    public record RecentExecution(
            OffsetDateTime executedAt,
            String ticker,
            BigDecimal executionQuantity,
            String executionType
    ) {
    }
}
