package com.woorifisa.won_invest_channel_server.domain.admin.external;

import com.woorifisa.won_invest_channel_server.domain.admin.dto.response.AdminAutoInvestExecutionListResponse;
import com.woorifisa.won_invest_channel_server.global.config.InternalFeignConfig;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "invest-core-admin-auto-invest-api",
        url = "${internal.services.invest-core.base-url}",
        configuration = InternalFeignConfig.class
)
public interface InvestCoreAdminAutoInvestApi {

    @GetMapping("/internal/admin/invest/auto-invest/executions")
    ApiResponse<AdminAutoInvestExecutionListResponse> getExecutions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userUuid,
            @RequestParam(required = false) Long executionId,
            @RequestParam(required = false) Long sweepRequestId,
            @RequestParam(required = false) String ticker,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );
}
