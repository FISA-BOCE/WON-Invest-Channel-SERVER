package com.woorifisa.won_invest_channel_server.domain.account.dto.response;

import java.util.List;
import java.util.UUID;

public record InternalInvestAccountsResponse(
        List<Account> accounts
) {

    public record Account(
            UUID investAccountUuid,
            String accountNoDisplay,
            String accountStatus
    ) {
    }
}
