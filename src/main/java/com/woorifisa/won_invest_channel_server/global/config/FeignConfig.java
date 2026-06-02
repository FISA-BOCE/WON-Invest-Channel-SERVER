package com.woorifisa.won_invest_channel_server.global.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoder.Default();
    }

    @Bean
    public RequestInterceptor internalApiRequestInterceptor(
            @Value("${internal.auth.service-id}") String serviceId,
            @Value("${internal.auth.api-key}") String apiKey
    ) {
        return template -> {
            template.header("X-Service-ID", serviceId);
            template.header("X-Internal-Api-Key", apiKey);
        };
    }
}
