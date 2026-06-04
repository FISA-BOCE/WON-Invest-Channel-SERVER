package com.woorifisa.won_invest_channel_server.domain.account.external.code;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountExternalErrorCode {

    COMMON_USER_MAPPING_NOT_FOUND("MAP_404_001"),
    CORE_INVALID_INPUT("INVEST_400_001"),
    CORE_ACCOUNT_ALREADY_CONNECTED("INVEST_400_003"),
    CORE_ACCOUNT_PERSISTENCE_FAILED("INVEST_500_001");

    private final String code;

    public static Optional<AccountExternalErrorCode> fromCode(String code) {
        return Arrays.stream(values())
                .filter(errorCode -> errorCode.code.equals(code))
                .findFirst();
    }
}
