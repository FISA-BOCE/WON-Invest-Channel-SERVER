package com.woorifisa.won_invest_channel_server.domain.account.api;

import com.woorifisa.won_invest_channel_server.domain.account.dto.request.LinkAccountRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.response.LinkAccountResponse;
import com.woorifisa.won_invest_channel_server.domain.account.service.InvestAccountService;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
@Tag(name = "Account", description = "증권계좌 연결 API")
public class InvestAccountApi {

    private final InvestAccountService investAccountService;

    @Operation(summary = "증권계좌 연결", description = "증권계좌를 연결하는 API입니다.")
    @PostMapping("/link")
    public ResponseEntity<ApiResponse<LinkAccountResponse>> linkAccount(
            @AuthenticationPrincipal String userUuid,
            @Valid @RequestBody LinkAccountRequest request
    ) {
        LinkAccountResponse response = investAccountService.linkAccount(userUuid, request);
        return ResponseEntity
                .status(SuccessStatus.ACCOUNT_LINKED.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.ACCOUNT_LINKED, response));
    }
}
