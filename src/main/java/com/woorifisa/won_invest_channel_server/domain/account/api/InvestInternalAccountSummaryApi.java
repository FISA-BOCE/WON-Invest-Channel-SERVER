package com.woorifisa.won_invest_channel_server.domain.account.api;

import com.woorifisa.won_invest_channel_server.domain.account.dto.request.InternalUpsertInvestAccountSummaryRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.response.InternalUpsertInvestAccountSummaryResponse;
import com.woorifisa.won_invest_channel_server.domain.account.service.InvestAccountService;
import com.woorifisa.won_invest_channel_server.domain.auth.exception.code.AuthErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.response.SuccessStatus;
import com.woorifisa.won_invest_channel_server.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "Invest Internal Account Summary", description = "증권계좌 summary 내부 동기화 API")
public class InvestInternalAccountSummaryApi {

    private final InvestAccountService investAccountService;

    @Operation(
            summary = "증권 계좌 summary upsert - 내부 API",
            description = "증권 코어 원장 변경 시 내부 서비스가 X-Service-ID, X-Internal-Api-Key, X-User-UUID 헤더로 호출하는 증권 계좌 summary upsert API입니다."
    )
    @SecurityRequirement(name = "SERVICE_ID")
    @SecurityRequirement(name = "INTERNAL_API_KEY")
    @PutMapping("/internal/invest/accounts/summary")
    public ResponseEntity<ApiResponse<InternalUpsertInvestAccountSummaryResponse>> upsertInternalAccountSummary(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Parameter(description = "증권 계좌 UUID", required = true)
            @RequestParam UUID investAccountUuid,
            @Valid @RequestBody InternalUpsertInvestAccountSummaryRequest request
    ) {
        AuthenticatedUser internalUser = requireAuthenticatedUser(authenticatedUser);
        validateUserConsistency(internalUser, request);

        InternalUpsertInvestAccountSummaryResponse response =
                investAccountService.upsertAccountSummary(investAccountUuid, request);

        return ResponseEntity
                .status(SuccessStatus.INVEST_ACCOUNT_SUMMARY_UPSERTED.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.INVEST_ACCOUNT_SUMMARY_UPSERTED, response));
    }

    private AuthenticatedUser requireAuthenticatedUser(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new BusinessException(AuthErrorCode.AUTHENTICATION_REQUIRED);
        }
        return authenticatedUser;
    }

    private void validateUserConsistency(
            AuthenticatedUser authenticatedUser,
            InternalUpsertInvestAccountSummaryRequest request
    ) {
        if (!authenticatedUser.userUuid().equals(request.userUuid())) {
            throw new BusinessException(AuthErrorCode.FORBIDDEN);
        }
    }
}
