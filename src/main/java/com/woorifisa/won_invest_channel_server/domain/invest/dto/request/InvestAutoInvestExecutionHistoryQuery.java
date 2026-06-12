package com.woorifisa.won_invest_channel_server.domain.invest.dto.request;

import java.time.OffsetDateTime;

public record InvestAutoInvestExecutionHistoryQuery(
        OffsetDateTime from,
        OffsetDateTime to,
        String status,
        String ticker,
        String cursor,
        Integer size
) {
    private static final int DEFAULT_SIZE = 20;

    public int normalizedSize() {
        return size == null ? DEFAULT_SIZE : size;
    }

    public String normalizedStatus() {
        return normalize(status);
    }

    public String normalizedTicker() {
        String normalized = normalize(ticker);
        return normalized == null ? null : normalized.toUpperCase();
    }

    public String normalizedCursor() {
        return normalize(cursor);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
