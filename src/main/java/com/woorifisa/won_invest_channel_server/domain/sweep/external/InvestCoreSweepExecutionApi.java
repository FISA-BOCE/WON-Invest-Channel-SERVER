package com.woorifisa.won_invest_channel_server.domain.sweep.external;

import com.woorifisa.won_invest_channel_server.domain.sweep.dto.request.InvestCoreSweepExecutionRequest;
import com.woorifisa.won_invest_channel_server.domain.sweep.dto.response.InvestCoreSweepExecutionResponse;
import com.woorifisa.won_invest_channel_server.global.config.InternalFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "invest-core-sweep-execution-api",
        url = "${internal.services.invest-core.base-url}",
        configuration = InternalFeignConfig.class
)
public interface InvestCoreSweepExecutionApi {

    @PostMapping("/internal/invest/sweep-executions")
    InvestCoreSweepExecutionResponse executeSweep(
            @RequestBody InvestCoreSweepExecutionRequest request
    );
}
