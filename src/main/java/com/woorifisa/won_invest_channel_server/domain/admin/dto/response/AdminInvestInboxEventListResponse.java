package com.woorifisa.won_invest_channel_server.domain.admin.dto.response;

import java.util.List;

public record AdminInvestInboxEventListResponse(
        AdminInvestInboxEventSummaryResponse summary,
        List<AdminInvestInboxEventItemResponse> items,
        int page,
        int size,
        long totalCount,
        int totalPages
) {
}
