package com.woorifisa.won_invest_channel_server.domain.invest.service;

import com.woorifisa.won_invest_channel_server.domain.invest.dto.response.InvestEtfHoldingsResponse;
import com.woorifisa.won_invest_channel_server.domain.invest.exception.code.InvestErrorCode;
import com.woorifisa.won_invest_channel_server.domain.invest.external.InvestCoreEtfQueryApi;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import feign.FeignException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvestCoreEtfQueryClient {

    private final InvestCoreEtfQueryApi investCoreEtfQueryApi;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public InvestEtfHoldingsResponse fetchCoreEtfHoldings(UUID userUuid, UUID accountUuid) {
        ApiResponse<InvestEtfHoldingsResponse> coreResponse;
        try {
            coreResponse = investCoreEtfQueryApi.getAccountEtfHoldings(userUuid, accountUuid);
        } catch (FeignException.NotFound e) {
            log.warn("Invest Core account not found [status={}]", e.status());
            throw new BusinessException(InvestErrorCode.ACCOUNT_NOT_FOUND, e);
        } catch (FeignException.Forbidden e) {
            log.warn("Invest Core account ownership denied [status={}]", e.status());
            throw new BusinessException(InvestErrorCode.ACCOUNT_NOT_OWNER, e);
        } catch (FeignException.BadRequest e) {
            log.warn("Invest Core account invalid status or bad request [status={}]", e.status());
            throw new BusinessException(InvestErrorCode.INVALID_ACCOUNT_STATUS, e);
        } catch (FeignException e) {
            log.warn("Invest Core ETF holdings query failed [status={}]", e.status());
            throw new BusinessException(InvestErrorCode.INTERNAL_QUERY_FAILED, e);
        }

        if (coreResponse == null || coreResponse.status() != 200 || coreResponse.data() == null) {
            log.warn("Invest Core ETF holdings response invalid [status={}, code={}]",
                    coreResponse == null ? null : coreResponse.status(),
                    coreResponse == null ? null : coreResponse.code());
            throw new BusinessException(InvestErrorCode.INTERNAL_QUERY_FAILED);
        }

        return coreResponse.data();
    }
}
