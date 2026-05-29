package com.woorifisa.won_invest_channel_server.domain.etf.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertyKisAccessTokenProvider implements KisAccessTokenProvider {

    private final String accessToken;

    //
    public PropertyKisAccessTokenProvider(
            @Value("${external.kis.access-token:}") String accessToken
    ) {
        this.accessToken = accessToken;
    }

    @Override
    public String getAccessToken() {
        if (!hasText(accessToken)) {
            throw new IllegalStateException("KIS Access Token 설정이 없습니다.");
        }

        return accessToken;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}