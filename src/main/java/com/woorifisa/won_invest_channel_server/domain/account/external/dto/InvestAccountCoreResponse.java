package com.woorifisa.won_invest_channel_server.domain.account.external.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record InvestAccountCoreResponse(
        UUID investAccountUuid,
        UUID investUserUuid,
        String accountNoDisplay,
        String accountStatus,
        String investConnectedStatus,
        LocalDateTime openedAt
) {}
