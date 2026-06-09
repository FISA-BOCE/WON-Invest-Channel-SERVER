package com.woorifisa.won_invest_channel_server.domain.etf.service;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.InvestEtfProductDetailResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.InternalInvestEtfDetailResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.exception.EtfSyncException;
import com.woorifisa.won_invest_channel_server.domain.etf.exception.code.EtfErrorCode;
import com.woorifisa.won_invest_channel_server.domain.etf.model.InvestChnEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.repository.InvestChnEtfProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestEtfProductQueryService {

    private final InvestChnEtfProductRepository investChnEtfProductRepository;

    public InvestEtfProductDetailResponse getEtfProductDetail(Long etfId) {
        return InvestEtfProductDetailResponse.from(findEtfProductById(etfId));
    }

    public InternalInvestEtfDetailResponse getInternalEtfProductDetail(Long etfId) {
        return InternalInvestEtfDetailResponse.from(findEtfProductById(etfId));
    }

    private InvestChnEtfProduct findEtfProductById(Long etfId) {
        return investChnEtfProductRepository.findById(etfId)
                .orElseThrow(() -> new EtfSyncException(EtfErrorCode.ETF_PRODUCT_NOT_FOUND));
    }
}
