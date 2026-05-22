package com.woorifisa.won_invest_channel_server.domain.account.api;

import com.woorifisa.won_invest_channel_server.domain.account.service.InvestAccountService;
import com.woorifisa.won_invest_channel_server.domain.account.dto.request.LinkAccountRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.response.LinkAccountResponse;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.response.SuccessStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class InvestAccountApi {

    private final InvestAccountService investAccountService;

    @PostMapping("/link")
    public ResponseEntity<ApiResponse<LinkAccountResponse>> linkAccount(
        @AuthenticationPrincipal String userUuid,
        @RequestBody @Valid LinkAccountRequest request
    ) {
        LinkAccountResponse response = investAccountService.linkAccount(userUuid, request);
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.ACCOUNT_LINKED, response));
    }
}
