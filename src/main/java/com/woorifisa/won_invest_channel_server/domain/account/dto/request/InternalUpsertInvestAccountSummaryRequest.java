package com.woorifisa.won_invest_channel_server.domain.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record InternalUpsertInvestAccountSummaryRequest(
        @NotNull
        UUID investUserUuid,

        @NotNull
        UUID userUuid,

        @NotBlank
        String accountNoDisplay,

        @NotBlank
        @Size(max = 50, message = "예금주명은 50자 이하여야 합니다.")
        String accountHolderName,

        @NotBlank
        String accountStatus
) {
}
