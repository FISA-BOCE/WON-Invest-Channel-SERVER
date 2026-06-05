package com.woorifisa.won_invest_channel_server.domain.aidb.service;

import com.woorifisa.won_invest_channel_server.domain.aidb.dto.request.AiDbQueryRequest;
import com.woorifisa.won_invest_channel_server.domain.aidb.dto.request.AiDbQueryType;
import com.woorifisa.won_invest_channel_server.domain.aidb.dto.response.AiDbHoldingListResponse;
import com.woorifisa.won_invest_channel_server.domain.aidb.dto.response.AiDbHoldingSummaryResponse;
import com.woorifisa.won_invest_channel_server.domain.aidb.dto.response.AiDbNoHoldingsResponse;
import com.woorifisa.won_invest_channel_server.domain.aidb.exception.code.AiDbErrorCode;
import com.woorifisa.won_invest_channel_server.domain.aidb.repository.InvestChnAiSummaryRepository;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiDbQueryService {

    private final InvestChnAiSummaryRepository investChnAiSummaryRepository;

    public Object query(AiDbQueryRequest request) {
        return switch (AiDbQueryType.from(request.queryType())) {
            case ETF_LIST -> getMyEtfHoldings(request);
            case ETF_AMOUNT -> getMyEtfBalanceSummary(request);
        };
    }

    private Object getMyEtfHoldings(AiDbQueryRequest request) {
        try {
            return investChnAiSummaryRepository.findHoldingListResponseByUserUuid(request.userUuid())
                    .map(response -> (Object) response)
                    .orElseGet(AiDbNoHoldingsResponse::of);
        } catch (CannotGetJdbcConnectionException e) {
            throw new BusinessException(AiDbErrorCode.MYSQL_CONNECTION_FAILED, e);
        } catch (DataAccessException e) {
            throw new BusinessException(AiDbErrorCode.MYSQL_QUERY_FAILED, e);
        }
    }

    private AiDbHoldingSummaryResponse getMyEtfBalanceSummary(AiDbQueryRequest request) {
        try {
            return investChnAiSummaryRepository.findHoldingSummaryResponseByUserUuid(request.userUuid())
                    .orElseThrow(() -> new BusinessException(AiDbErrorCode.QUERY_RESULT_NOT_FOUND));
        } catch (CannotGetJdbcConnectionException e) {
            throw new BusinessException(AiDbErrorCode.MYSQL_CONNECTION_FAILED, e);
        } catch (DataAccessException e) {
            throw new BusinessException(AiDbErrorCode.MYSQL_QUERY_FAILED, e);
        }
    }
}
