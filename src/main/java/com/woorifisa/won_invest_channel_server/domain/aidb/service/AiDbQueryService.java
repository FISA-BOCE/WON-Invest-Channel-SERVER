package com.woorifisa.won_invest_channel_server.domain.aidb.service;

import com.woorifisa.won_invest_channel_server.domain.aidb.dto.request.AiDbQueryRequest;
import com.woorifisa.won_invest_channel_server.domain.aidb.dto.request.AiDbQueryType;
import com.woorifisa.won_invest_channel_server.domain.aidb.dto.response.AiDbHoldingListResponse;
import com.woorifisa.won_invest_channel_server.domain.aidb.dto.response.AiDbHoldingSummaryResponse;
import com.woorifisa.won_invest_channel_server.domain.aidb.exception.code.AiDbErrorCode;
import com.woorifisa.won_invest_channel_server.domain.aidb.repository.InvestChnAiSummaryRepository;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiDbQueryService {

    private final InvestChnAiSummaryRepository investChnAiSummaryRepository;

    public Object query(AiDbQueryRequest request) {
        return switch (AiDbQueryType.from(request.queryType())) {
            case MY_ETF_HOLDINGS -> getMyEtfHoldings(request);
            case MY_ETF_BALANCE_SUMMARY -> getMyEtfBalanceSummary(request);
        };
    }

    private AiDbHoldingListResponse getMyEtfHoldings(AiDbQueryRequest request) {
        List<InvestChnAiSummaryRepository.HoldingListRow> rows;
        try {
            rows = investChnAiSummaryRepository.findHoldingListRowsByUserUuid(request.userUuid().toString());
        } catch (DataAccessException e) {
            throw new BusinessException(AiDbErrorCode.MYSQL_QUERY_FAILED, e);
        }

        if (rows.isEmpty()) {
            throw new BusinessException(AiDbErrorCode.QUERY_RESULT_NOT_FOUND);
        }

        InvestChnAiSummaryRepository.HoldingListRow first = rows.get(0);
        List<AiDbHoldingListResponse.Holding> holdings = rows.stream()
                .map(row -> new AiDbHoldingListResponse.Holding(
                        row.getEtfId(),
                        row.getTicker(),
                        row.getEtfName(),
                        row.getHoldingQuantity(),
                        row.getEvaluationAmount(),
                        row.getLastSyncedAt()
                ))
                .toList();

        return new AiDbHoldingListResponse(
                first.getUserUuid(),
                first.getInvestAccountUuid(),
                holdings.size(),
                holdings
        );
    }

    private AiDbHoldingSummaryResponse getMyEtfBalanceSummary(AiDbQueryRequest request) {
        List<InvestChnAiSummaryRepository.HoldingSummaryRow> rows;
        try {
            rows = investChnAiSummaryRepository.findHoldingSummaryRowsByUserUuid(request.userUuid().toString());
        } catch (DataAccessException e) {
            throw new BusinessException(AiDbErrorCode.MYSQL_QUERY_FAILED, e);
        }

        if (rows.isEmpty()) {
            throw new BusinessException(AiDbErrorCode.QUERY_RESULT_NOT_FOUND);
        }

        InvestChnAiSummaryRepository.HoldingSummaryRow first = rows.get(0);
        List<AiDbHoldingSummaryResponse.Holding> holdings = rows.stream()
                .filter(row -> row.getEtfId() != null)
                .map(row -> new AiDbHoldingSummaryResponse.Holding(
                        row.getEtfId(),
                        row.getTicker(),
                        row.getEtfName(),
                        row.getHoldingQuantity(),
                        row.getAverageBuyPrice(),
                        row.getEvaluationAmount(),
                        row.getProfitLossAmount(),
                        row.getProfitLossRate(),
                        row.getHoldingLastSyncedAt()
                ))
                .toList();

        return new AiDbHoldingSummaryResponse(
                first.getUserUuid(),
                first.getInvestAccountUuid(),
                first.getTotalBuyAmount(),
                first.getTotalEvaluationAmount(),
                first.getTotalProfitLossAmount(),
                first.getTotalProfitLossRate(),
                holdings.size(),
                first.getSummaryLastSyncedAt(),
                holdings
        );
    }
}
