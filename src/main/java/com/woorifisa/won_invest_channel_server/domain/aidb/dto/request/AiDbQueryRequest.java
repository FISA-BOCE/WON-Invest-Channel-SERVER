package com.woorifisa.won_invest_channel_server.domain.aidb.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AiDbQueryRequest(
        @NotNull
        UUID userUuid,

        @NotBlank
        String queryType
) {
}
