package com.woorifisa.won_invest_channel_server.domain.invest.external;

import com.woorifisa.won_invest_channel_server.domain.invest.dto.response.InvestEtfHoldingsResponse;
import com.woorifisa.won_invest_channel_server.global.config.FeignConfig;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "invest-core-etf-query-api",
        url = "${feign.invest-core.url}",
        configuration = FeignConfig.class
)
public interface InvestCoreEtfQueryApi {

    @GetMapping("/internal/invest/accounts/{accountUuid}/etfs")
    ApiResponse<InvestEtfHoldingsResponse> getAccountEtfHoldings(
            @RequestHeader("X-User-UUID") UUID userUuid,
            @PathVariable UUID accountUuid
    );
}
