package com.woorifisa.won_invest_channel_server.domain.account.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LinkAccountRequest(@NotBlank String investAccountUuid) {}
