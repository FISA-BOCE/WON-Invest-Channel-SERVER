package com.woorifisa.won_invest_channel_server.domain.etf.api;

import com.woorifisa.won_invest_channel_server.domain.auth.exception.code.AuthErrorCode;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.InvestEtfProductListResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.InvestEtfProductDetailResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.service.InvestEtfProductQueryService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "Invest ETF Product", description = "ETF 상품 조회 API")
public class InvestEtfProductApi {

    private final InvestEtfProductQueryService investEtfProductQueryService;

    @Operation(
            summary = "제공 ETF 목록 조회",
            description = "자동투자 대상으로 제공 가능한 ETF 목록을 조회합니다."
    )
    @GetMapping("/api/invest/etfs")
    public ResponseEntity<ApiResponse<InvestEtfProductListResponse>> getEtfProducts(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Parameter(description = "트랜잭션 추적용 ID")
            @RequestHeader(value = "X-Transaction-ID", required = false) String transactionId
    ) {
        requireAuthenticatedUser(authenticatedUser);

        InvestEtfProductListResponse response = investEtfProductQueryService.getEtfProducts();

        return ResponseEntity
                .status(SuccessStatus.ETF_PRODUCT_LIST_FOUND.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.ETF_PRODUCT_LIST_FOUND, response));
    }

    @Operation(
            summary = "ETF 상품 상세 조회",
            description = "ETF 상품 마스터에서 ETF명, 티커, 시장, 통화, 위험등급을 조회합니다."
    )
    @GetMapping("/api/invest/etfs/{etfId}")
    public ResponseEntity<ApiResponse<InvestEtfProductDetailResponse>> getEtfProductDetail(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Parameter(description = "트랜잭션 추적용 ID")
            @RequestHeader(value = "X-Transaction-ID", required = false) String transactionId,
            @Parameter(description = "조회할 ETF 내부 ID", required = true)
            @PathVariable Long etfId
    ) {
        requireAuthenticatedUser(authenticatedUser);

        InvestEtfProductDetailResponse response = investEtfProductQueryService.getEtfProductDetail(etfId);

        return ResponseEntity
                .status(SuccessStatus.ETF_PRODUCT_DETAIL_FOUND.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.ETF_PRODUCT_DETAIL_FOUND, response));
    }

    private AuthenticatedUser requireAuthenticatedUser(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new BusinessException(AuthErrorCode.AUTHENTICATION_REQUIRED);
        }
        return authenticatedUser;
    }
}
