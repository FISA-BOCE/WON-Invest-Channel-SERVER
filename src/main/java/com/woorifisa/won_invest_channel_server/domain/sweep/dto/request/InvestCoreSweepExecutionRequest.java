package com.woorifisa.won_invest_channel_server.domain.sweep.dto.request;

import com.woorifisa.won_invest_channel_server.domain.sweep.dto.event.SweepRequestedEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record InvestCoreSweepExecutionRequest(
        @NotBlank
        String eventId,

        @NotBlank
        String eventType,

        @NotBlank
        String correlationId,

        @NotBlank
        String idempotencyKey,

        @NotNull
        Long sweepRequestId,

        @NotBlank
        String userUuid,

        @NotBlank
        String cardUserUuid,

        @NotNull
        Long performanceId,

        @NotNull
        Long pointLedgerId,

        @NotBlank
        String baseMonth,

        @NotNull
        @Positive
        Long pointAmount,

        @NotNull
        @Positive
        Long krwAmount,

        @NotNull
        Long etfId,

        @NotNull
        LocalDateTime requestedAt
) {
    public static InvestCoreSweepExecutionRequest from(SweepRequestedEvent event) {
        return new InvestCoreSweepExecutionRequest(
                event.eventId(),
                event.eventType(),
                event.correlationId(),
                event.idempotencyKey(),
                event.sweepRequestId(),
                event.userUuid(),
                event.cardUserUuid(),
                event.performanceId(),
                event.pointLedgerId(),
                event.baseMonth(),
                event.pointAmount(),
                event.krwAmount(),
                event.etfId(),
                event.requestedAt()
        );
    }
}
