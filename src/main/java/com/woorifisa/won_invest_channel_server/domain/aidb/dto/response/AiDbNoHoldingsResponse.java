package com.woorifisa.won_invest_channel_server.domain.aidb.dto.response;

public record AiDbNoHoldingsResponse(
        int status,
        String code,
        String message,
        Object data
) {

    public static AiDbNoHoldingsResponse of() {
        return new AiDbNoHoldingsResponse(
                200,
                "CHAT_200_001",
                "보유한 종목이 존재하지 않습니다.",
                null
        );
    }
}
