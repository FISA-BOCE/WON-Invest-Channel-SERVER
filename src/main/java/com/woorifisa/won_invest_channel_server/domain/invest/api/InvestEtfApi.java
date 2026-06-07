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
@Tag(name = "Invest ETF", description = "보유 ETF 조회 API")
public class InvestEtfApi {

    private final InvestEtfQueryService investEtfQueryService;

    @Operation(
            summary = "보유 ETF 상세 조회",
            description = "JWT 인증 후 본인 명의의 ACTIVE 증권계좌에 대해 보유 ETF, 총 평가 금액, 최근 매수 체결 3건을 조회합니다."
    )
    @GetMapping("/api/invest/accounts/{accountUuid}/etfs")
    public ResponseEntity<ApiResponse<InvestEtfHoldingsResponse>> getAccountEtfs(
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
