package com.woorifisa.won_invest_channel_server.domain.aidb.dto.request;

import com.woorifisa.won_invest_channel_server.domain.aidb.exception.code.AiDbErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;

import java.util.Arrays;

public enum AiDbQueryType {
    MY_ETF_HOLDINGS,
    MY_ETF_BALANCE_SUMMARY;

    public static AiDbQueryType from(String value) {
        return Arrays.stream(values())
                .filter(queryType -> queryType.name().equals(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(AiDbErrorCode.UNSUPPORTED_QUERY_TYPE));
    }
}
