package com.woorifisa.won_invest_channel_server.domain.etf.api;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.EtfProductSyncResult;
import com.woorifisa.won_invest_channel_server.domain.etf.service.InvestChnEtfProductSyncService;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.response.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InternalEtfProductSyncApi {

    private final InvestChnEtfProductSyncService investChnEtfProductSyncService;

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
