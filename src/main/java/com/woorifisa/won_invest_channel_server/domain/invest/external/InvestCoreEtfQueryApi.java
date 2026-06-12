package com.woorifisa.won_invest_channel_server.domain.invest.external;

import com.woorifisa.won_invest_channel_server.domain.invest.dto.response.InvestAutoInvestExecutionHistoryResponse;
import com.woorifisa.won_invest_channel_server.domain.invest.dto.response.InvestEtfHoldingsResponse;
import com.woorifisa.won_invest_channel_server.global.config.InternalFeignConfig;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "invest-core-etf-query-api",
        url = "${internal.services.invest-core.base-url}",
        configuration = InternalFeignConfig.class
)
public interface InvestCoreEtfQueryApi {

    @GetMapping("/internal/invest/accounts/{accountUuid}/etfs")
    ApiResponse<InvestEtfHoldingsResponse> getAccountEtfHoldings(
            @RequestHeader("X-User-UUID") UUID userUuid,
            @PathVariable UUID accountUuid
    );

    @GetMapping("/internal/invest/accounts/{accountUuid}/auto-invest/executions")
    ApiResponse<InvestAutoInvestExecutionHistoryResponse> getAutoInvestExecutionHistories(
            @RequestHeader("X-User-UUID") UUID userUuid,
            @PathVariable UUID accountUuid,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String ticker,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size
    );
}
