package com.woorifisa.won_invest_channel_server.domain.account.external;

import com.woorifisa.won_invest_channel_server.domain.account.external.dto.LinkInvestMappingRequest;
import com.woorifisa.won_invest_channel_server.domain.account.external.dto.MappingStatusResponse;
import com.woorifisa.won_invest_channel_server.global.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "common-mapping-api", url = "${feign.common-server-url}", configuration = FeignConfig.class)
public interface CommonMappingApi {

    @GetMapping("/internal/mappings/users/{userUuid}")
    MappingStatusResponse getMappingStatus(@PathVariable("userUuid") String userUuid);

    @PatchMapping("/internal/mappings/users/{userUuid}/invest")
    void linkInvestMapping(
        @PathVariable("userUuid") String userUuid,
        @RequestBody LinkInvestMappingRequest request
    );
}
