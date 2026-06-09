package com.woorifisa.won_invest_channel_server.domain.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record InternalUpsertInvestAccountSummaryRequest(
        @NotNull
        UUID investUserUuid,

        @NotNull
        UUID userUuid,

        @NotBlank
        String accountNoDisplay,

        @NotBlank
        String accountStatus
) {
}
