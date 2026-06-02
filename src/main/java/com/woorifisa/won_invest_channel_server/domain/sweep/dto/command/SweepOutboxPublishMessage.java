package com.woorifisa.won_invest_channel_server.domain.sweep.dto.command;

import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SweepOutboxPublishMessage(
        @NotNull
        Long outboxEventId,

        @NotBlank
        String eventId,

        @NotNull
        SweepEventType eventType,

        @NotNull
        Long sweepRequestId,

        @NotBlank
        String correlationId,

        @NotBlank
        String idempotencyKey,

        @NotBlank
        String payload
) {
}
