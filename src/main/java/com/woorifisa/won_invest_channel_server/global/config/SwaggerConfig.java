package com.woorifisa.won_invest_channel_server.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String JWT_SECURITY_SCHEME = "JWT";
    private static final String SERVICE_ID_SECURITY_SCHEME = "SERVICE_ID";
    private static final String INTERNAL_API_KEY_SECURITY_SCHEME = "INTERNAL_API_KEY";

    @Bean
    public OpenAPI openAPI() {
        Components components = new Components()
                .addSecuritySchemes(JWT_SECURITY_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization"))
                .addSecuritySchemes(SERVICE_ID_SECURITY_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Service-ID"))
                .addSecuritySchemes(INTERNAL_API_KEY_SECURITY_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Internal-Api-Key"));

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(JWT_SECURITY_SCHEME);

        return new OpenAPI()
                .info(new Info()
                        .title("INVEST-CHANNEL API 명세서")
                        .version("v1.0"))
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
