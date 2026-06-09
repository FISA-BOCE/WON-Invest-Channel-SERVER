package com.woorifisa.won_invest_channel_server.domain.invest.api;

import com.woorifisa.won_invest_channel_server.domain.auth.exception.code.AuthErrorCode;
import com.woorifisa.won_invest_channel_server.domain.invest.dto.response.InvestEtfHoldingsResponse;
import com.woorifisa.won_invest_channel_server.domain.invest.service.InvestEtfQueryService;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.response.SuccessStatus;
import com.woorifisa.won_invest_channel_server.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "Invest Internal ETF", description = "보유 ETF 내부 조회 API")
public class InvestInternalEtfApi {

    private final InvestEtfQueryService investEtfQueryService;

    @Operation(
            summary = "보유 ETF 상세 조회 - 내부 API",
            description = "내부 서비스가 X-Service-ID, X-Internal-Api-Key, X-User-UUID 헤더로 호출하는 보유 ETF 상세 조회 API입니다."
    )
    @SecurityRequirement(name = "SERVICE_ID")
    @SecurityRequirement(name = "INTERNAL_API_KEY")
    @GetMapping("/internal/invest/accounts/{accountUuid}/etfs")
    public ResponseEntity<ApiResponse<InvestEtfHoldingsResponse>> getInternalAccountEtfs(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Parameter(description = "조회할 증권계좌 UUID", required = true)
            @PathVariable UUID accountUuid
    ) {
        InvestEtfHoldingsResponse response = investEtfQueryService.getAccountEtfs(
                requireAuthenticatedUser(authenticatedUser).userUuid(),
                accountUuid
        );
        return ResponseEntity
                .status(SuccessStatus.INVEST_ACCOUNT_ETF_FOUND.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.INVEST_ACCOUNT_ETF_FOUND, response));
    }

    private AuthenticatedUser requireAuthenticatedUser(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new BusinessException(AuthErrorCode.AUTHENTICATION_REQUIRED);
        }
        return authenticatedUser;
    }
}
