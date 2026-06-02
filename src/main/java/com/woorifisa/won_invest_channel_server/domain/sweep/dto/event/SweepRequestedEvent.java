package com.woorifisa.won_invest_channel_server.domain.sweep.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record SweepRequestedEvent(
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
}
