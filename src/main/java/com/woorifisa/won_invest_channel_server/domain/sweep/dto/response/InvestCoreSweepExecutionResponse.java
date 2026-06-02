package com.woorifisa.won_invest_channel_server.domain.sweep.dto.response;

import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepExecutionStatus;

public record InvestCoreSweepExecutionResponse(
        Long sweepExecutionId,
        String idempotencyKey,
        SweepExecutionStatus status,
        String failureCode,
        String failureMessage
) {
    public boolean completed() {
        return SweepExecutionStatus.COMPLETED == status;
    }

    public boolean failed() {
        return SweepExecutionStatus.FAILED == status;
    }
}
