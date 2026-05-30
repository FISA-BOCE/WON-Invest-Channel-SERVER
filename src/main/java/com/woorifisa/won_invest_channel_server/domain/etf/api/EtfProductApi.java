package com.woorifisa.won_invest_channel_server.domain.etf.api;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.EtfProductListResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfRiskGrade;
import com.woorifisa.won_invest_channel_server.domain.etf.service.InvestChnEtfProductQueryService;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.response.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EtfProductApi {

    private final InvestChnEtfProductQueryService investChnEtfProductQueryService;

    @GetMapping("/api/invest/etfs")
    public ResponseEntity<ApiResponse<EtfProductListResponse>> getEtfProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String market,
            @RequestParam(required = false) EtfCurrency currency,
            @RequestParam(required = false) EtfRiskGrade riskGrade
    ) {
        EtfProductListResponse response =
                investChnEtfProductQueryService.getProvidedEtfProducts(
                        keyword,
                        market,
                        currency,
                        riskGrade
                );

        return ResponseEntity
                .status(SuccessStatus.ETF_PRODUCT_LIST_FOUND.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.ETF_PRODUCT_LIST_FOUND, response));
    }
}
