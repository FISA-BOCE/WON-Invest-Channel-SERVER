package com.woorifisa.won_invest_channel_server.domain.account.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record LinkAccountResponse(
        UUID investAccountUuid,
        String accountNoDisplay,
        String accountStatus,
        boolean investConnectedStatus,
        LocalDateTime linkedAt
) {}
