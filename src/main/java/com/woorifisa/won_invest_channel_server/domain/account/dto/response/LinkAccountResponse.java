package com.woorifisa.won_invest_channel_server.domain.account.dto.response;

import com.woorifisa.won_invest_channel_server.domain.account.model.AccountStatus;

import java.time.LocalDateTime;

public record LinkAccountResponse(
        String investAccountUuid,
        String accountNoDisplay,
        AccountStatus accountStatus,
        boolean investConnectedStatus,
        LocalDateTime linkedAt
) {}
