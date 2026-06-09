package com.woorifisa.won_invest_channel_server.domain.etf.external;

import com.woorifisa.won_invest_channel_server.domain.etf.exception.code.EtfErrorCode;
import com.woorifisa.won_invest_channel_server.domain.etf.exception.EtfSyncException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertyKisAccessTokenProvider implements KisAccessTokenProvider {

    private final String accessToken;

    public PropertyKisAccessTokenProvider(
            @Value("${external.kis.access-token:}") String accessToken
    ) {
        this.accessToken = accessToken == null ? null : accessToken.trim();
    }

    @Override
    public String getAccessToken() {
        if (!hasText(accessToken)) {
            throw new EtfSyncException(EtfErrorCode.KIS_ACCESS_TOKEN_NOT_CONFIGURED);
        }

        return accessToken;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
