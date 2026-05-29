package com.woorifisa.won_invest_channel_server.domain.etf.client;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.kis.response.KisOverseasProductInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KisOverseasProductInfoClient {

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

    public KisOverseasProductInfoClient(
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
                    throw new IllegalStateException(
                            "KIS 해외주식 상품기본정보 API 호출 실패. status="
                                    + httpResponse.getStatusCode()
                                    + ", ticker="
                                    + ticker
                    );
                })
                .body(KisOverseasProductInfoResponse.class);

        return extractResponse(response, ticker);
    }

    private void validateRequest(String productTypeCode, String ticker) {
        if (!hasText(productTypeCode)) {
            throw new IllegalArgumentException("KIS 상품유형코드가 없습니다.");
        }

        if (!hasText(ticker)) {
            throw new IllegalArgumentException("KIS 상품번호가 없습니다.");
        }
    }

    private void validateConfig() {
        if (!hasText(appKey)) {
            throw new IllegalStateException("KIS appKey 설정이 없습니다.");
        }

        if (!hasText(appSecret)) {
            throw new IllegalStateException("KIS appSecret 설정이 없습니다.");
        }
    }

    private KisOverseasProductInfoResponse extractResponse(
            KisOverseasProductInfoResponse response,
            String ticker
    ) {
        if (response == null) {
            throw new IllegalStateException(
                    "KIS 해외주식 상품기본정보 API 응답이 없습니다. ticker=" + ticker
            );
        }

        if (!response.isSuccess()) {
            throw new IllegalStateException(
                    "KIS 해외주식 상품기본정보 API 응답 실패. ticker="
                            + ticker
                            + ", rtCd="
                            + response.rtCd()
                            + ", msgCd="
                            + response.msgCd()
                            + ", msg="
                            + response.msg1()
            );
        }

        if (response.output() == null) {
            throw new IllegalStateException(
                    "KIS 해외주식 상품기본정보 API output이 없습니다. ticker=" + ticker
            );
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