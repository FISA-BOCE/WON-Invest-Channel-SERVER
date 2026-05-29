package com.woorifisa.won_invest_channel_server.domain.sweep.dto.response;

public record InvestCoreSweepExecutionResponse(
        Long sweepExecutionId,
        String idempotencyKey,
        String status,
        String failureCode,
        String failureMessage
) {
    public boolean completed() {
        return "COMPLETED".equals(status);
    }
}
