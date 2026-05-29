package com.woorifisa.won_invest_channel_server.domain.sweep.model.enums;

public enum SweepOutboxPublishStatus {
    PENDING,
    PROCESSING,
    RETRY,
    PUBLISHED,
    FAILED
}
