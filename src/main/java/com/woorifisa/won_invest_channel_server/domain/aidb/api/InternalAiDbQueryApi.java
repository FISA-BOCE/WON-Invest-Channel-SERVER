package com.woorifisa.won_invest_channel_server.domain.aidb.api;

import com.woorifisa.won_invest_channel_server.domain.aidb.dto.request.AiDbQueryRequest;
import com.woorifisa.won_invest_channel_server.domain.aidb.service.AiDbQueryService;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "AI DB Query", description = "AI DB MySQL 조회 API")
public class InternalAiDbQueryApi {

    private final AiDbQueryService aiDbQueryService;

    @Operation(
            summary = "AI DB MySQL 조회",
            description = "내부 서비스에서 queryType에 따라 MySQL 투자 데이터를 조회합니다."
    )
    @PostMapping("/internal/invest/db/mysql/query")
    public ResponseEntity<ApiResponse<Object>> query(
            @Parameter(description = "호출 서비스 식별자", required = true)
            @RequestHeader("X-Service-ID") String serviceId,
            @NotNull @Valid @RequestBody(required = true) AiDbQueryRequest request
    ) {
        Object response = aiDbQueryService.query(request);

        return ResponseEntity
                .status(SuccessStatus.OK.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.OK, response));
    }
}
