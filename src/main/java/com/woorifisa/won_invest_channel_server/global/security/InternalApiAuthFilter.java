package com.woorifisa.won_invest_channel_server.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorifisa.won_invest_channel_server.domain.auth.exception.code.AuthErrorCode;
import com.woorifisa.won_invest_channel_server.global.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class InternalApiAuthFilter extends OncePerRequestFilter {

    private static final String INTERNAL_PATH_PREFIX = "/internal/";
    private static final String SERVICE_ID_HEADER = "X-Service-ID";
    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";
    private static final String USER_UUID_HEADER = "X-User-UUID";

    private final ObjectMapper objectMapper;
    private final String expectedServiceId;
    private final String expectedInternalApiKey;

    public InternalApiAuthFilter(
            ObjectMapper objectMapper,
            @Value("${internal.allowed-service-id:}") String expectedServiceId,
            @Value("${internal.api-key:}") String expectedInternalApiKey
    ) {
        this.objectMapper = objectMapper;
        this.expectedServiceId = normalize(expectedServiceId);
        this.expectedInternalApiKey = normalize(expectedInternalApiKey);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(INTERNAL_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!hasText(expectedServiceId) || !hasText(expectedInternalApiKey)) {
            log.error("Channel 내부 API 인증 설정이 누락되었습니다. uri={}", request.getRequestURI());
            writeUnauthorizedResponse(response);
            return;
        }

        String serviceId = normalize(request.getHeader(SERVICE_ID_HEADER));
        String internalApiKey = normalize(request.getHeader(INTERNAL_API_KEY_HEADER));

        if (!expectedServiceId.equals(serviceId) || !expectedInternalApiKey.equals(internalApiKey)) {
            log.warn("내부 API 인증 실패. uri={}, serviceId={}", request.getRequestURI(), serviceId);
            writeUnauthorizedResponse(response);
            return;
        }

        String userUuidHeader = normalize(request.getHeader(USER_UUID_HEADER));
        if (!hasText(userUuidHeader)) {
            log.warn("내부 API 사용자 식별 헤더가 올바르지 않습니다. uri={}, userUuid={}", request.getRequestURI(), userUuidHeader);
            writeUnauthorizedResponse(response);
            return;
        }

        try {
            UUID userUuid = UUID.fromString(userUuidHeader);
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(userUuid, userUuid, "internal");
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(authenticatedUser, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (IllegalArgumentException e) {
            log.warn("내부 API 사용자 식별 헤더가 올바르지 않습니다. uri={}, userUuid={}", request.getRequestURI(), userUuidHeader);
            writeUnauthorizedResponse(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(AuthErrorCode.AUTHENTICATION_REQUIRED.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), ErrorResponse.of(AuthErrorCode.AUTHENTICATION_REQUIRED));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}
