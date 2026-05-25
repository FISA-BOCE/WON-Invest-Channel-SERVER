package com.woorifisa.won_invest_channel_server.domain.account.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CreateInvestAccountResponse(
        UUID investAccountUuid,
        String accountNoDisplay,
        String accountStatus,
        String investConnectedStatus,
        Instant openedAt
) {}