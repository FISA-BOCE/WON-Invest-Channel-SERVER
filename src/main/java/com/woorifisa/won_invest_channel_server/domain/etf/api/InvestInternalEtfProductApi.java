package com.woorifisa.won_invest_channel_server.domain.etf.api;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.InternalInvestEtfDetailResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.service.InvestEtfProductQueryService;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "Invest Internal ETF Product", description = "ETF 상품 내부 조회 API")
public class InvestInternalEtfProductApi {

    private final InvestEtfProductQueryService investEtfProductQueryService;

    @Operation(
            summary = "ETF 상품 단건 조회 - 내부 API",
            description = "내부 서비스가 X-Service-ID, X-Internal-Api-Key 헤더로 호출하는 ETF 상품 단건 검증 조회 API입니다."
    )
    @SecurityRequirement(name = "SERVICE_ID")
    @SecurityRequirement(name = "INTERNAL_API_KEY")
    @GetMapping("/internal/invest/etfs/{etfId}")
    public ResponseEntity<ApiResponse<InternalInvestEtfDetailResponse>> getInternalEtfProductDetail(
            @PathVariable @Positive Long etfId
    ) {
        InternalInvestEtfDetailResponse response = investEtfProductQueryService.getInternalEtfProductDetail(etfId);

        return ResponseEntity
                .status(SuccessStatus.ETF_PRODUCT_DETAIL_FOUND.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.ETF_PRODUCT_DETAIL_FOUND, response));
    }
}
