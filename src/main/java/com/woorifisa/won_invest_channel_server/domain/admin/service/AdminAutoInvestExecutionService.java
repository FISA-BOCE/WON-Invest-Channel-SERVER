package com.woorifisa.won_invest_channel_server.domain.admin.service;

import com.woorifisa.won_invest_channel_server.domain.admin.dto.response.AdminAutoInvestExecutionListResponse;
import com.woorifisa.won_invest_channel_server.domain.admin.external.InvestCoreAdminAutoInvestApi;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAutoInvestExecutionService {

    private final InvestCoreAdminAutoInvestApi investCoreAdminAutoInvestApi;

    public AdminAutoInvestExecutionListResponse getExecutions(
            String status,
            String userUuid,
            Long executionId,
            Long sweepRequestId,
            String ticker,
            int page,
            int size
    ) {
        ApiResponse<AdminAutoInvestExecutionListResponse> response = investCoreAdminAutoInvestApi.getExecutions(
                status,
                userUuid,
                executionId,
                sweepRequestId,
                ticker,
                page,
                size
        );

        return response.data();
    }
}
