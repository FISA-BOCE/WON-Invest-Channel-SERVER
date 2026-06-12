package com.woorifisa.won_invest_channel_server.domain.admin.dto.response;

public record AdminInvestOutboxEventSummaryResponse(
        long totalCount,
        long publishedCount,
        long failedCount,
        long retryingCount,
        long pendingCount
) {
}
