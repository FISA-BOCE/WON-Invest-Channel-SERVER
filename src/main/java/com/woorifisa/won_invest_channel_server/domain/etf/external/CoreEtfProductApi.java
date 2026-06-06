package com.woorifisa.won_invest_channel_server.domain.etf.external;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.core.request.CoreEtfProductUpsertRequest;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.core.response.CoreApiResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.core.response.CoreEtfProductUpsertResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.exception.code.EtfErrorCode;
import com.woorifisa.won_invest_channel_server.domain.etf.exception.EtfSyncException;
import com.woorifisa.won_invest_channel_server.global.response.SuccessStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CoreEtfProductApi {

    private static final String INTERNAL_SYNC_PATH = "/internal/etf-products/sync";
    private static final String SERVICE_ID_HEADER = "X-Service-ID";
    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    private static final ParameterizedTypeReference<CoreApiResponse<CoreEtfProductUpsertResponse>> RESPONSE_TYPE =
            new ParameterizedTypeReference<CoreApiResponse<CoreEtfProductUpsertResponse>>() {
            };

    private final RestClient restClient;
    private final String serviceId;
    private final String internalApiKey;

    public CoreEtfProductApi(
            RestClient.Builder restClientBuilder,
            @Value("${internal.services.invest-core.base-url}") String coreBaseUrl,
            @Value("${internal.service-id}") String serviceId,
            @Value("${internal.api-key}") String internalApiKey
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
                    throw new EtfSyncException(EtfErrorCode.CORE_ETF_UPSERT_API_FAILED);
                })
                .body(RESPONSE_TYPE);

        return extractData(response);
    }

    // 요청값이 기본 조건을 만족하는지 확인하는 메서드 (예외처리)
    private void validateRequest(CoreEtfProductUpsertRequest request) {
        if (request == null) {
            throw new EtfSyncException(EtfErrorCode.CORE_ETF_UPSERT_REQUEST_EMPTY);
        }

        if (!hasText(request.externalProvider())) {
            throw new EtfSyncException(EtfErrorCode.CORE_ETF_UPSERT_REQUEST_EMPTY);
        }

        if (!hasText(request.ticker())) {
            throw new EtfSyncException(EtfErrorCode.CORE_ETF_UPSERT_REQUEST_EMPTY);
        }

        if (!hasText(request.etfName())) {
            throw new EtfSyncException(EtfErrorCode.CORE_ETF_UPSERT_REQUEST_EMPTY);
        }

        if (request.currency() == null) {
            throw new EtfSyncException(EtfErrorCode.CORE_ETF_UPSERT_REQUEST_EMPTY);
        }

        if (request.productStatus() == null) {
            throw new EtfSyncException(EtfErrorCode.CORE_ETF_UPSERT_REQUEST_EMPTY);
        }
    }

    private void validateConfig() {
        if (!hasText(serviceId)) {
            throw new EtfSyncException(EtfErrorCode.CORE_SERVICE_ID_NOT_CONFIGURED);
        }

        if (!hasText(internalApiKey)) {
            throw new EtfSyncException(EtfErrorCode.CORE_INTERNAL_API_KEY_NOT_CONFIGURED);
        }
    }

    // Core 응답에서 실제 필요한 data 꺼내는 메서드
    private CoreEtfProductUpsertResponse extractData(
            CoreApiResponse<CoreEtfProductUpsertResponse> response
    ) {
        if (response == null) {
            throw new EtfSyncException(EtfErrorCode.CORE_ETF_UPSERT_RESPONSE_EMPTY);
        }

        if (!isCoreEtfProductSyncSuccess(response)) {
            throw new EtfSyncException(EtfErrorCode.CORE_ETF_UPSERT_RESPONSE_FAILED);
        }

        if (response.data() == null) {
            throw new EtfSyncException(EtfErrorCode.CORE_ETF_UPSERT_RESPONSE_DATA_EMPTY);
        }

        if (response.data().etfId() == null) {
            throw new EtfSyncException(EtfErrorCode.CORE_ETF_UPSERT_RESPONSE_ETF_ID_EMPTY);
        }

        return response.data();
    }

    private boolean isCoreEtfProductSyncSuccess(
            CoreApiResponse<CoreEtfProductUpsertResponse> response
    ) {
        return Integer.valueOf(SuccessStatus.CORE_ETF_PRODUCT_SYNCED.getHttpStatus().value())
                .equals(response.status())
                && SuccessStatus.CORE_ETF_PRODUCT_SYNCED.getCode().equals(response.code());
    }

    // 문자열 비어있는지 확인하는 메서드
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
