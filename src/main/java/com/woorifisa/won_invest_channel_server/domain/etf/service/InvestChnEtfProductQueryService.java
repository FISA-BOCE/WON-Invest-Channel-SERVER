package com.woorifisa.won_invest_channel_server.domain.etf.service;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.EtfProductListResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.EtfProductSummaryResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.model.InvestChnEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfRiskGrade;
import com.woorifisa.won_invest_channel_server.domain.etf.repository.InvestChnEtfProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestChnEtfProductQueryService {

    private final InvestChnEtfProductRepository investChnEtfProductRepository;

    public EtfProductListResponse getProvidedEtfProducts(
            String keyword,
            String market,
            EtfCurrency currency,
            EtfRiskGrade riskGrade
    ) {
        List<EtfProductSummaryResponse> etfs = investChnEtfProductRepository.findAll().stream()
                .filter(this::isProvidedProduct)
                .filter(product -> matchesKeyword(product, keyword))
                .filter(product -> matchesMarket(product, market))
                .filter(product -> matchesCurrency(product, currency))
                .filter(product -> matchesRiskGrade(product, riskGrade))
                .sorted(Comparator
                        .comparing(
                                InvestChnEtfProduct::getDisplayOrder,
                                Comparator.nullsLast(Integer::compareTo)
                        )
                        .thenComparing(InvestChnEtfProduct::getEtfId)
                )
                .map(EtfProductSummaryResponse::from)
                .toList();

        return EtfProductListResponse.of(etfs);
    }

    private boolean isProvidedProduct(InvestChnEtfProduct product) {
        return Boolean.TRUE.equals(product.getIsTradeAvailable())
                && Boolean.TRUE.equals(product.getIsFractionalAvailable())
                && product.getCurrency() == EtfCurrency.USD;
    }

    private boolean matchesKeyword(InvestChnEtfProduct product, String keyword) {
        if (!hasText(keyword)) {
            return true;
        }

        String normalizedKeyword = keyword.trim().toLowerCase();

        return containsIgnoreCase(product.getTicker(), normalizedKeyword)
                || containsIgnoreCase(product.getEtfName(), normalizedKeyword)
                || containsIgnoreCase(product.getDescription(), normalizedKeyword);
    }

    private boolean matchesMarket(InvestChnEtfProduct product, String market) {
        if (!hasText(market)) {
            return true;
        }

        return product.getMarket() != null
                && product.getMarket().equalsIgnoreCase(market.trim());
    }

    private boolean matchesCurrency(InvestChnEtfProduct product, EtfCurrency currency) {
        if (currency == null) {
            return true;
        }

        return product.getCurrency() == currency;
    }

    private boolean matchesRiskGrade(InvestChnEtfProduct product, EtfRiskGrade riskGrade) {
        if (riskGrade == null) {
            return true;
        }

        return product.getRiskGrade() == riskGrade;
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
