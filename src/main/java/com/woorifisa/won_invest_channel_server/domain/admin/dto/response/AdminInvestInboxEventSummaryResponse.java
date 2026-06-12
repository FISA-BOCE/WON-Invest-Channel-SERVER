package com.woorifisa.won_invest_channel_server.domain.admin.dto.response;

public record AdminInvestInboxEventSummaryResponse(
        long totalCount,
        long processedCount,
        long failedCount,
        long processingCount,
        long receivedCount
) {
}
