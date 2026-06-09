package com.woorifisa.won_invest_channel_server.domain.account.external.dto;

public record MappingStatusResponse(InvestStatus invest) {

    public record InvestStatus(boolean isConnected) {}
}
