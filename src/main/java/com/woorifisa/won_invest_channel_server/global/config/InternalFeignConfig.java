package com.woorifisa.won_invest_channel_server.global.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalFeignConfig {

    private static final String SERVICE_ID_HEADER = "X-Service-ID";
    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    private final String serviceId;
    private final String internalApiKey;

    public InternalFeignConfig(
            @Value("${internal.service-id}") String serviceId,
            @Value("${internal.api-key}") String internalApiKey
    ) {
        this.serviceId = normalize(serviceId);
        this.internalApiKey = normalize(internalApiKey);
        validateInternalAuthProperties();
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoder.Default();
    }

    @Bean
    public RequestInterceptor internalApiAuthRequestInterceptor() {
        return template -> {
            template.header(SERVICE_ID_HEADER, serviceId);
            template.header(INTERNAL_API_KEY_HEADER, internalApiKey);
        };
    }

    private void validateInternalAuthProperties() {
        if (!hasText(serviceId) || !hasText(internalApiKey)) {
            throw new IllegalStateException("internal.service-id and internal.api-key must not be blank.");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
