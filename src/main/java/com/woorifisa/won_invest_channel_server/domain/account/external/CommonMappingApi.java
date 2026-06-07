package com.woorifisa.won_invest_channel_server.domain.account.external;

import com.woorifisa.won_invest_channel_server.domain.account.external.dto.LinkInvestMappingRequest;
import com.woorifisa.won_invest_channel_server.domain.account.external.dto.MappingStatusResponse;
import com.woorifisa.won_invest_channel_server.global.config.InternalFeignConfig;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "common-mapping-api", url = "${internal.services.common.base-url}", configuration = InternalFeignConfig.class)
public interface CommonMappingApi {

    @GetMapping("/internal/mappings/users/{userUuid}")
    ApiResponse<MappingStatusResponse> getMappingStatus(@PathVariable("userUuid") UUID userUuid);

    @PatchMapping("/internal/mappings/users/{userUuid}/invest")
    ApiResponse<MappingStatusResponse> linkInvestMapping(
            @PathVariable("userUuid") UUID userUuid,
            @RequestBody LinkInvestMappingRequest request
    );
}
