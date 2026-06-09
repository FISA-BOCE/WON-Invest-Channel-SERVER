package com.woorifisa.won_invest_channel_server.domain.account.dto.response;

import java.util.UUID;

public record InternalUpsertInvestAccountSummaryResponse(
        UUID investAccountUuid,
        UUID investUserUuid,
        UUID userUuid,
        String accountNoDisplay,
        String accountStatus
) {
}
