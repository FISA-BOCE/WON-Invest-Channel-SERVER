package com.woorifisa.won_invest_channel_server.domain.account.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LinkAccountRequest(@NotNull UUID investAccountUuid) {}
