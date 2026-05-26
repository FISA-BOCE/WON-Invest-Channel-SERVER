package com.woorifisa.won_invest_channel_server.domain.account.api;

import com.woorifisa.won_invest_channel_server.domain.account.dto.request.CreateInvestAccountRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.response.CreateInvestAccountResponse;
import com.woorifisa.won_invest_channel_server.domain.account.dto.request.LinkAccountRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.response.LinkAccountResponse;
import com.woorifisa.won_invest_channel_server.domain.account.service.InvestAccountService;
import com.woorifisa.won_invest_channel_server.global.exception.code.CommonErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.response.SuccessStatus;
import com.woorifisa.won_invest_channel_server.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invest/accounts")
@RequiredArgsConstructor
@Tag(name = "Account", description = "증권계좌 API")
public class InvestAccountApi {

    private final InvestAccountService investAccountService;

    @Operation(summary = "신규 증권계좌 개설", description = "신규 증권계좌를 개설하는 API입니다.")
    @PostMapping("/new")
    public ResponseEntity<ApiResponse<CreateInvestAccountResponse>> createInvestAccount(
            @Valid @RequestBody CreateInvestAccountRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        CreateInvestAccountResponse response =
                investAccountService.openNewInvestAccount(request, authorizationHeader);
        return ResponseEntity
                .status(SuccessStatus.ACCOUNT_CREATED.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.ACCOUNT_CREATED, response));
    }

    @Operation(summary = "증권계좌 연결", description = "증권계좌를 연결하는 API입니다.")
    @PostMapping("/link")
    public ResponseEntity<ApiResponse<LinkAccountResponse>> linkAccount(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody LinkAccountRequest request
    ) {
        if (authenticatedUser == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        LinkAccountResponse response = investAccountService.linkAccount(authenticatedUser.userUuid(), request);
        return ResponseEntity
                .status(SuccessStatus.ACCOUNT_LINKED.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.ACCOUNT_LINKED, response));
    }
}