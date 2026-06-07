package com.woorifisa.won_invest_channel_server.domain.etf.api;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.EtfProductSyncResult;
import com.woorifisa.won_invest_channel_server.domain.etf.service.InvestChnEtfProductSyncService;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "internal-etf-product-sync-api")
public class InternalEtfProductSyncApi {

    private final InvestChnEtfProductSyncService investChnEtfProductSyncService;

    @Operation(
            summary = "ETF 상품 동기화"
    )
    @PostMapping("/internal/invest/etf-products/sync")
    public ResponseEntity<ApiResponse<EtfProductSyncResult>> syncEtfProducts(
    ) {
        EtfProductSyncResult result =
                investChnEtfProductSyncService.syncCuratedEtfProducts();

        return ResponseEntity.ok(
                ApiResponse.of(
                        SuccessStatus.ETF_PRODUCT_SYNC_SUCCESS,
                        result
                )
        );
    }
}
