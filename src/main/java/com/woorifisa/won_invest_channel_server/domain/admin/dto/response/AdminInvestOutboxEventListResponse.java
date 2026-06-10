package com.woorifisa.won_invest_channel_server.domain.admin.dto.response;

import java.util.List;

public record AdminInvestOutboxEventListResponse(
        AdminInvestOutboxEventSummaryResponse summary,
        List<AdminInvestOutboxEventItemResponse> items,
        int page,
        int size,
        long totalCount,
        int totalPages
) {
}
