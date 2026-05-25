package com.woorifisa.won_invest_channel_server.domain.account.external;

import com.woorifisa.won_invest_channel_server.domain.account.dto.request.CreateInvestAccountRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;
import java.util.UUID;

@FeignClient(name = "invest-core", url = "${feign.invest-core.url}")
public interface InvestCoreAccountApi {

    record CoreAccountData(
            UUID investAccountUuid,
            String accountNoDisplay,
            String accountStatus,
            String investConnectedStatus,
            Instant openedAt
    ) {}

    record CoreApiResponse(
            int status,
            String message,
            CoreAccountData data
    ) {}

    @PostMapping("/internal/invest/accounts/new")
    CoreApiResponse openNewInvestAccount(@RequestBody CreateInvestAccountRequest request);
}