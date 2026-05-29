package com.woorifisa.won_invest_channel_server.domain.etf.client;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.core.request.CoreEtfProductUpsertRequest;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.core.response.CoreApiResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.core.response.CoreEtfProductUpsertResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CoreEtfProductClient {

    private static final String INTERNAL_SYNC_PATH = "/internal/etf-products/sync";
    private static final String SERVICE_ID_HEADER = "X-Service-ID";
    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";
    private static final String CORE_ETF_PRODUCT_SYNC_SUCCESS_CODE = "ETF_200_001";

    private static final ParameterizedTypeReference<CoreApiResponse<CoreEtfProductUpsertResponse>> RESPONSE_TYPE =
            new ParameterizedTypeReference<CoreApiResponse<CoreEtfProductUpsertResponse>>() {
            };

    private final RestClient restClient;
    private final String serviceId;
    private final String internalApiKey;

    public CoreEtfProductClient(
            RestClient.Builder restClientBuilder,
            @Value("${external.core.base-url:http://localhost:8081}") String coreBaseUrl,
            @Value("${external.core.service-id:won-channel}") String serviceId,
            @Value("${external.core.internal-api-key:}") String internalApiKey
    ) {
        this.restClient = restClientBuilder
                .baseUrl(coreBaseUrl)
                .build();
        this.serviceId = serviceId;
        this.internalApiKey = internalApiKey;
    }

    // Core 서버에 ETF 상품 저장/갱신 요청을 보내고 -> Core 응답의 data를 반환
    public CoreEtfProductUpsertResponse upsertEtfProduct(CoreEtfProductUpsertRequest request) {
        // 요청 검증
        validateRequest(request);
        validateConfig();

        // HTTP POST 요청
        CoreApiResponse<CoreEtfProductUpsertResponse> response = restClient.post()
                .uri(INTERNAL_SYNC_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .header(SERVICE_ID_HEADER, serviceId)
                .header(INTERNAL_API_KEY_HEADER, internalApiKey)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (httpRequest, httpResponse) -> {
                    throw new IllegalStateException(
                            "Core ETF 상품 동기화 API 호출 실패. status=" + httpResponse.getStatusCode()
                    );
                })
                .body(RESPONSE_TYPE);

        return extractData(response);
    }

    // 요청값이 기본 조건을 만족하는지 확인하는 메서드 (예외처리)
    private void validateRequest(CoreEtfProductUpsertRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Core ETF 상품 동기화 요청 정보가 없습니다.");
        }

        if (!hasText(request.externalProvider())) {
            throw new IllegalArgumentException("외부 제공자 정보가 없습니다.");
        }

        if (!hasText(request.ticker())) {
            throw new IllegalArgumentException("ETF 티커가 없습니다.");
        }

        if (!hasText(request.etfName())) {
            throw new IllegalArgumentException("ETF 상품명이 없습니다.");
        }

        if (request.currency() == null) {
            throw new IllegalArgumentException("ETF 통화 정보가 없습니다.");
        }

        if (request.productStatus() == null) {
            throw new IllegalArgumentException("ETF 상품 상태 정보가 없습니다.");
        }
    }

    private void validateConfig() {
        if (!hasText(serviceId)) {
            throw new IllegalStateException("Core 내부 API Service ID 설정이 없습니다.");
        }

        if (!hasText(internalApiKey)) {
            throw new IllegalStateException("Core 내부 API Key 설정이 없습니다.");
        }
    }

    // Core 응답에서 실제 필요한 data 꺼내는 메서드
    private CoreEtfProductUpsertResponse extractData(
            CoreApiResponse<CoreEtfProductUpsertResponse> response
    ) {
        if (response == null) {
            throw new IllegalStateException("Core ETF 상품 동기화 API 응답이 없습니다.");
        }


        if (!isCoreEtfProductSyncSuccess(response)) {
            throw new IllegalStateException(
                    "Core ETF 상품 동기화 API 응답 실패. code="
                            + response.code()
                            + ", message="
                            + response.message()
            );
        }

        if (response.data() == null) {
            throw new IllegalStateException("Core ETF 상품 동기화 API 응답 data가 없습니다.");
        }

        if (response.data().etfId() == null) {
            throw new IllegalStateException("Core ETF 상품 동기화 API 응답 etfId가 없습니다.");
        }

        return response.data();
    }

    // status = 200, code = ETF_200_001 이면 통과
    private boolean isCoreEtfProductSyncSuccess(
            CoreApiResponse<CoreEtfProductUpsertResponse> response
    ) {
        return Integer.valueOf(200).equals(response.status())
                && CORE_ETF_PRODUCT_SYNC_SUCCESS_CODE.equals(response.code());
    }

    // 문자열 비어있는지 확인하는 메서드
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}