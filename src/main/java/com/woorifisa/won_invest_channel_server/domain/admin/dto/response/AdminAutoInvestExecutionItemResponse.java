package com.woorifisa.won_invest_channel_server.domain.admin.dto.response;

import java.time.LocalDateTime;

public record AdminAutoInvestExecutionItemResponse(
        Long executionId,
        Long sweepRequestId,
        String userUuid,
        Long etfId,
        String ticker,
        String executionStatus,
        String fxStatus,
        String orderStatus,
        String failReason,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime updatedAt
) {
}
