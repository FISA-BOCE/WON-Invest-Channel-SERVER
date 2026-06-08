package com.woorifisa.won_invest_channel_server.domain.account.api;

import com.woorifisa.won_invest_channel_server.domain.account.dto.response.InternalInvestAccountsResponse;
import com.woorifisa.won_invest_channel_server.domain.account.service.InvestAccountService;
import com.woorifisa.won_invest_channel_server.domain.auth.exception.code.AuthErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.response.SuccessStatus;
import com.woorifisa.won_invest_channel_server.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "Invest Internal Account", description = "증권계좌 내부 조회 API")
public class InvestInternalAccountApi {

    private final InvestAccountService investAccountService;

    @Operation(
            summary = "증권 계좌 목록 조회 - 내부 API",
            description = "내부 서비스가 X-Service-ID, X-Internal-Api-Key, X-User-UUID 헤더로 호출하는 증권 계좌 목록 조회 API입니다."
    )
    @SecurityRequirement(name = "SERVICE_ID")
    @SecurityRequirement(name = "INTERNAL_API_KEY")
    @GetMapping("/internal/invest/accounts")
    public ResponseEntity<ApiResponse<InternalInvestAccountsResponse>> getInternalAccounts(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        InternalInvestAccountsResponse response =
                investAccountService.getInternalAccounts(requireAuthenticatedUser(authenticatedUser).userUuid());
        return ResponseEntity
                .status(SuccessStatus.INVEST_ACCOUNT_LIST_FOUND.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.INVEST_ACCOUNT_LIST_FOUND, response));
    }

    private AuthenticatedUser requireAuthenticatedUser(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new BusinessException(AuthErrorCode.AUTHENTICATION_REQUIRED);
        }
        return authenticatedUser;
    }
}
