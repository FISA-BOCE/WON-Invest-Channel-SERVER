package com.woorifisa.won_invest_channel_server.domain.admin.dto.response;

import com.woorifisa.won_invest_channel_server.domain.sweep.model.InvestChnInboxEvent;

import java.time.LocalDateTime;

public record AdminInvestInboxEventItemResponse(
        Long inboxId,
        String systemType,
        Long sweepRequestId,
        String sourceEventId,
        String eventType,
        String processStatus,
        int retryCount,
        String lastErrorMessage,
        String failReason,
        LocalDateTime receivedAt,
        LocalDateTime processedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AdminInvestInboxEventItemResponse from(InvestChnInboxEvent inbox) {
        return new AdminInvestInboxEventItemResponse(
                inbox.getInboxEventId(),
                "INVEST",
                inbox.getSweepRequestId(),
                inbox.getSourceEventId(),
                inbox.getEventType().name(),
                inbox.getProcessStatus().name(),
                inbox.getRetryCount(),
                inbox.getLastErrorMessage(),
                inbox.getLastErrorMessage(),
                inbox.getReceivedAt(),
                inbox.getProcessedAt(),
                inbox.getCreatedAt(),
                inbox.getUpdatedAt()
        );
    }
}
