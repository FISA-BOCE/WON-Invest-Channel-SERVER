package com.woorifisa.won_invest_channel_server.domain.aidb.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AiDbHoldingListResponse(
        UUID userUuid,
        UUID investAccountUuid,
        Integer holdingCount,
        List<Holding> holdings
) {

    public record Holding(
            Long etfId,
            String ticker,
            String etfName,
            BigDecimal holdingQuantity,
            BigDecimal evaluationAmount,
            LocalDateTime lastSyncedAt
    ) {
    }
}
