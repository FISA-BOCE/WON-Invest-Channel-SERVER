package com.woorifisa.won_invest_channel_server.domain.invest.api;

import com.woorifisa.won_invest_channel_server.domain.auth.exception.code.AuthErrorCode;
import com.woorifisa.won_invest_channel_server.domain.invest.dto.response.InvestMainResponse;
import com.woorifisa.won_invest_channel_server.domain.invest.service.InvestMainQueryService;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.response.SuccessStatus;
import com.woorifisa.won_invest_channel_server.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "Invest Main", description = "투자 메인 화면 조회 API")
public class InvestMainApi {

    private final InvestMainQueryService investMainQueryService;

    @Operation(
            summary = "투자 메인 화면 조회",
            description = "JWT 인증 후 본인 명의의 대표 증권계좌 기준으로 메인 화면용 투자 자산 요약 정보를 조회합니다."
    )
    @GetMapping("/api/invest/main")
    public ResponseEntity<ApiResponse<InvestMainResponse>> getInvestMain(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Parameter(description = "트랜잭션 추적용 ID")
            @RequestHeader(value = "X-Transaction-ID", required = false) String transactionId
    ) {
        InvestMainResponse response = investMainQueryService.getInvestMain(
                requireAuthenticatedUser(authenticatedUser).userUuid()
        );

        return ResponseEntity
                .status(SuccessStatus.INVEST_MAIN_FOUND.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.INVEST_MAIN_FOUND, response));
    }

    private AuthenticatedUser requireAuthenticatedUser(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new BusinessException(AuthErrorCode.AUTHENTICATION_REQUIRED);
        }
        return authenticatedUser;
    }
}
