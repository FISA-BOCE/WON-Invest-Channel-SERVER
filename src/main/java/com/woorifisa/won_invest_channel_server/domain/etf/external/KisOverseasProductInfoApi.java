package com.woorifisa.won_invest_channel_server.domain.etf.external;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.kis.response.KisOverseasProductInfoResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.exception.code.EtfErrorCode;
import com.woorifisa.won_invest_channel_server.domain.etf.exception.EtfSyncException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KisOverseasProductInfoApi {

    private static final String SEARCH_INFO_PATH = "/uapi/overseas-price/v1/quotations/search-info";
    private static final String APP_KEY_HEADER = "appkey";
    private static final String APP_SECRET_HEADER = "appsecret";
    private static final String TR_ID_HEADER = "tr_id";
    private static final String CUSTOMER_TYPE_HEADER = "custtype";
    private static final String CONTENT_TYPE_VALUE = "application/json; charset=utf-8";
    private static final String PRODUCT_TYPE_CODE_PARAM = "PRDT_TYPE_CD";
    private static final String PRODUCT_NO_PARAM = "PDNO";
    private static final String TR_ID_OVERSEAS_PRODUCT_INFO = "CTPF1702R";
    private static final String CUSTOMER_TYPE_PERSONAL = "P";
    private final RestClient restClient;
    private final KisAccessTokenProvider accessTokenProvider;
    private final String appKey;
    private final String appSecret;

    public KisOverseasProductInfoApi(
            RestClient.Builder restClientBuilder,
            KisAccessTokenProvider accessTokenProvider,
            @Value("${external.kis.base-url:https://openapi.koreainvestment.com:9443}") String kisBaseUrl,
            @Value("${external.kis.app-key:}") String appKey,
            @Value("${external.kis.app-secret:}") String appSecret
    ) {
        this.restClient = restClientBuilder
                .baseUrl(kisBaseUrl)
                .build();
        this.accessTokenProvider = accessTokenProvider;
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    public KisOverseasProductInfoResponse getProductInfo(
            String productTypeCode,
            String ticker
    ) {
        validateRequest(productTypeCode, ticker);
        validateConfig();

        String accessToken = accessTokenProvider.getAccessToken();

        KisOverseasProductInfoResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(SEARCH_INFO_PATH)
                        .queryParam(PRODUCT_TYPE_CODE_PARAM, productTypeCode)
                        .queryParam(PRODUCT_NO_PARAM, ticker)
                        .build())
                .header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_VALUE)
                .header(HttpHeaders.AUTHORIZATION, toBearerToken(accessToken))
                .header(APP_KEY_HEADER, appKey)
                .header(APP_SECRET_HEADER, appSecret)
                .header(TR_ID_HEADER, TR_ID_OVERSEAS_PRODUCT_INFO)
                .header(CUSTOMER_TYPE_HEADER, CUSTOMER_TYPE_PERSONAL)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (httpRequest, httpResponse) -> {
                    throw new EtfSyncException(EtfErrorCode.KIS_PRODUCT_API_FAILED);
                })
                .body(KisOverseasProductInfoResponse.class);

        return extractResponse(response, ticker);
    }

    private void validateRequest(String productTypeCode, String ticker) {
        if (!hasText(productTypeCode)) {
            throw new EtfSyncException(EtfErrorCode.KIS_PRODUCT_TYPE_CODE_EMPTY);
        }

        if (!hasText(ticker)) {
            throw new EtfSyncException(EtfErrorCode.KIS_PRODUCT_NUMBER_EMPTY);
        }
    }

    private void validateConfig() {
        if (!hasText(appKey)) {
            throw new EtfSyncException(EtfErrorCode.KIS_APP_KEY_NOT_CONFIGURED);
        }

        if (!hasText(appSecret)) {
            throw new EtfSyncException(EtfErrorCode.KIS_APP_SECRET_NOT_CONFIGURED);
        }
    }

    private KisOverseasProductInfoResponse extractResponse(
            KisOverseasProductInfoResponse response,
            String ticker
    ) {
        if (response == null) {
            throw new EtfSyncException(EtfErrorCode.KIS_PRODUCT_RESPONSE_EMPTY);
        }

        if (!response.isSuccess()) {
            throw new EtfSyncException(EtfErrorCode.KIS_PRODUCT_RESPONSE_FAILED);
        }

        if (response.output() == null) {
            throw new EtfSyncException(EtfErrorCode.KIS_PRODUCT_OUTPUT_EMPTY);
        }

        return response;
    }

    private String toBearerToken(String accessToken) {
        if (accessToken.startsWith("Bearer ")) {
            return accessToken;
        }

        return "Bearer " + accessToken;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
