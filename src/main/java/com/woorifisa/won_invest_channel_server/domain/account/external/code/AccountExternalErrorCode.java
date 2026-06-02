package com.woorifisa.won_invest_channel_server.domain.account.external.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountExternalErrorCode {

    COMMON_USER_MAPPING_NOT_FOUND("MAP_404_001"),
    CORE_ACCOUNT_ALREADY_CONNECTED("INVEST_400_003"),
    CORE_ACCOUNT_PERSISTENCE_FAILED("INVEST_500_001");

    private final String code;
}
