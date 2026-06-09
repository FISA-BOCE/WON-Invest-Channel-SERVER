package com.woorifisa.won_invest_channel_server.domain.etf.service;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.InternalInvestEtfDetailResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.InvestEtfProductDetailResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.InvestEtfProductListResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.exception.EtfSyncException;
import com.woorifisa.won_invest_channel_server.domain.etf.exception.code.EtfErrorCode;
import com.woorifisa.won_invest_channel_server.domain.etf.model.InvestChnEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.repository.InvestChnEtfProductRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestEtfProductQueryService {

    private final InvestChnEtfProductRepository investChnEtfProductRepository;

    public InvestEtfProductListResponse getEtfProducts() {
        try {
            Map<String, InvestChnEtfProduct> latestProductsByTicker = investChnEtfProductRepository.findAll().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            InvestChnEtfProduct::getTicker,
                            product -> product,
                            this::selectLatestProduct
                    ));

            List<InvestEtfProductListResponse.EtfSummary> etfs = latestProductsByTicker.values().stream()
                    .filter(this::isAutoInvestAvailable)
                    .sorted(Comparator
                            .comparing(InvestChnEtfProduct::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                            .thenComparing(InvestChnEtfProduct::getTicker))
                    .map(InvestEtfProductListResponse.EtfSummary::from)
                    .toList();

            return new InvestEtfProductListResponse(etfs);
        } catch (DataAccessException e) {
            throw new EtfSyncException(EtfErrorCode.ETF_PRODUCT_QUERY_FAILED, e);
        }
    }

    public InvestEtfProductDetailResponse getEtfProductDetail(Long etfId) {
        return InvestEtfProductDetailResponse.from(findEtfProductById(etfId));
    }

    public InternalInvestEtfDetailResponse getInternalEtfProductDetail(Long etfId) {
        return InternalInvestEtfDetailResponse.from(findEtfProductById(etfId));
    }

    private InvestChnEtfProduct findEtfProductById(Long etfId) {
        try {
            return investChnEtfProductRepository.findById(etfId)
                    .orElseThrow(() -> new EtfSyncException(EtfErrorCode.ETF_PRODUCT_NOT_FOUND));
        } catch (DataAccessException e) {
            throw new EtfSyncException(EtfErrorCode.ETF_PRODUCT_QUERY_FAILED, e);
        }
    }

    private boolean isAutoInvestAvailable(InvestChnEtfProduct product) {
        return Boolean.TRUE.equals(product.getIsTradeAvailable())
                && Boolean.TRUE.equals(product.getIsFractionalAvailable())
                && EtfCurrency.USD == product.getCurrency();
    }

    private InvestChnEtfProduct selectLatestProduct(InvestChnEtfProduct left, InvestChnEtfProduct right) {
        return Comparator.comparing(
                        InvestChnEtfProduct::getLastSyncedAt,
                        Comparator.nullsFirst(Comparator.naturalOrder())
                )
                .thenComparing(InvestChnEtfProduct::getEtfId)
                .compare(left, right) >= 0 ? left : right;
    }
}
