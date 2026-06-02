package com.woorifisa.won_invest_channel_server.global.config;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "sweep.outbox.publisher")
public record SweepOutboxPublisherProperties(
        boolean enabled,

        @Positive
        int batchSize,

        @Positive
        int maxRetryCount,

        @Positive
        long fixedDelayMs
) {
}
