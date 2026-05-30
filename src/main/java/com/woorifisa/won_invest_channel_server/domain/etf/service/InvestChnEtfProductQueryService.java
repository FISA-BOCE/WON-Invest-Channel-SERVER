package com.woorifisa.won_invest_channel_server.domain.etf.service;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.EtfProductListResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.EtfProductSummaryResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.repository.InvestChnEtfProductRepository;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfRiskGrade;
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
        List<EtfProductSummaryResponse> etfs = investChnEtfProductRepository
                .findProvidedEtfProducts(
                        normalize(keyword),
                        normalize(market),
                        currency,
                        riskGrade
                )
                .stream()
                .map(EtfProductSummaryResponse::from)
                .toList();

        return EtfProductListResponse.of(etfs);
    }

    private String normalize(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}