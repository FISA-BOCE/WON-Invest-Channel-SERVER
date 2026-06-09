package com.woorifisa.won_invest_channel_server.domain.account.external;

import com.woorifisa.won_invest_channel_server.domain.account.dto.request.CreateInvestAccountRequest;
import com.woorifisa.won_invest_channel_server.domain.account.external.dto.InvestAccountCoreResponse;
import com.woorifisa.won_invest_channel_server.global.config.InternalFeignConfig;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "invest-core", url = "${internal.services.invest-core.base-url}", configuration = InternalFeignConfig.class)
public interface InvestCoreAccountApi {

    @PostMapping("/internal/invest/accounts/new")
    ApiResponse<InvestAccountCoreResponse> createNewInvestAccount(
            @RequestHeader("X-User-UUID") UUID userUuid,
            @RequestBody CreateInvestAccountRequest request
    );
}
