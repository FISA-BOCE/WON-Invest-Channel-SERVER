package com.woorifisa.won_invest_channel_server.global.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sweep.consumer")
public record SweepRequestConsumerProperties(
        boolean enabled,

        @Min(1)
        @Max(10)
        int maxMessages,

        @Min(0)
        @Max(20)
        int waitTimeSeconds,

        @Min(3)
        long processingTimeoutSeconds
) {
}
