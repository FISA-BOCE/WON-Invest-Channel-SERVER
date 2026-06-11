package com.woorifisa.won_invest_channel_server.domain.admin.dto.response;

public record AdminAutoInvestExecutionSummaryResponse(
        long totalCount,
        long exchangeCompletedCount,
        long orderFailedCount,
        long completedCount
) {
}
