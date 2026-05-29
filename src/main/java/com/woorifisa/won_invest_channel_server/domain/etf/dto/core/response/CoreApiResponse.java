package com.woorifisa.won_invest_channel_server.domain.etf.dto.core.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Core 서버 공통 응답 DTO- Core 응답의 data 영역은 제네릭으로 받음
@JsonIgnoreProperties(ignoreUnknown = true)
public record CoreApiResponse<T>(

        Integer status,

        String code,

        @JsonProperty("message")
        @JsonAlias("message")
        String message,

        T data
) {

    public boolean isSuccess() {
        return Integer.valueOf(200).equals(status)
                && "SUCCESS".equals(code);
    }
}