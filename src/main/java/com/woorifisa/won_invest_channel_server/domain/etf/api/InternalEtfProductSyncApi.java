package com.woorifisa.won_invest_channel_server.domain.etf.api;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.EtfProductSyncResult;
import com.woorifisa.won_invest_channel_server.domain.etf.service.InvestChnEtfProductService;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.response.SuccessStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class InternalEtfProductSyncApi {

    private static final String SERVICE_ID_HEADER = "X-Service-ID";
    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    private final InvestChnEtfProductService investChnEtfProductService;
    private final String expectedServiceId;
    private final String expectedInternalApiKey;

    public InternalEtfProductSyncApi(
            InvestChnEtfProductService investChnEtfProductService,
            @Value("${internal.channel.allowed-service-id:won-batch}") String expectedServiceId,
            @Value("${internal.channel.api-key:}") String expectedInternalApiKey
    ) {
        this.investChnEtfProductService = investChnEtfProductService;
        this.expectedServiceId = expectedServiceId;
        this.expectedInternalApiKey = expectedInternalApiKey;
    }

    @PostMapping("/internal/invest/etf-products/sync")
    public ResponseEntity<ApiResponse<EtfProductSyncResult>> syncEtfProducts(
            @RequestHeader(value = SERVICE_ID_HEADER, required = false) String serviceId,
            @RequestHeader(value = INTERNAL_API_KEY_HEADER, required = false) String internalApiKey
    ) {

        validateInternalAuth(serviceId, internalApiKey);

        EtfProductSyncResult result =
                investChnEtfProductService.syncCuratedEtfProducts();

        return ResponseEntity.ok(
                ApiResponse.of(
                        SuccessStatus.ETF_PRODUCT_SYNC_SUCCESS,
                        result
                )
        );
    }

    private void validateInternalAuth(String serviceId, String internalApiKey) {
        if (!hasText(expectedInternalApiKey)) {
            throw new IllegalStateException("Channel 내부 API Key 설정이 없습니다.");
        }

        if (!hasText(serviceId) || !expectedServiceId.equals(serviceId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        if (!hasText(internalApiKey) || !expectedInternalApiKey.equals(internalApiKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}