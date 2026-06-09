package com.woorifisa.won_invest_channel_server.domain.account.dto.response;

import com.woorifisa.won_invest_channel_server.domain.account.model.InvestChnAccountSummary;
import java.util.UUID;

public record InternalInvestAccountDetailResponse(
        UUID investAccountUuid,
        UUID userUuid,
        String accountStatus
) {

    public static InternalInvestAccountDetailResponse from(InvestChnAccountSummary accountSummary) {
        return new InternalInvestAccountDetailResponse(
                accountSummary.getInvestAccountUuid(),
                accountSummary.getUserUuid(),
                accountSummary.getAccountStatus().name()
        );
    }
}
