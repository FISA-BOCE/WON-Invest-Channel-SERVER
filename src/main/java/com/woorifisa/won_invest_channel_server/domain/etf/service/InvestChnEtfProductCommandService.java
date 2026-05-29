package com.woorifisa.won_invest_channel_server.domain.etf.service;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.ExternalEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.model.InvestChnEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.repository.InvestChnEtfProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvestChnEtfProductCommandService {

    private final InvestChnEtfProductRepository investChnEtfProductRepository;

    // Core 에서 etf_id 받아서 Channel DB 조회
    @Transactional
    public void upsertFromCoreEtfId(Long etfId, ExternalEtfProduct product) {
        if (etfId == null) {
            throw new IllegalArgumentException("Core ETF ID가 없습니다.");
        }

        if (product == null) {
            throw new IllegalArgumentException("Channel ETF 저장 대상 상품 정보가 없습니다.");
        }

        LocalDateTime lastSyncedAt = LocalDateTime.now();

        InvestChnEtfProduct chnEtfProduct = investChnEtfProductRepository.findById(etfId)
                // 있으면 update
                .map(existingProduct -> {
                    existingProduct.updateProductInfo(
                            product.externalProvider(),
                            product.externalEtfId(),
                            product.ticker(),
                            product.etfName(),
                            product.description(),
                            product.market(),
                            product.currency(),
                            product.riskGrade(),
                            product.isFractionalAvailable(),
                            product.isTradeAvailable(),
                            lastSyncedAt
                    );

                    existingProduct.updateDisplayOrder(product.displayOrder());

                    return existingProduct;
                })
                // 없으면 create
                .orElseGet(() -> InvestChnEtfProduct.create(
                        etfId,
                        product.externalProvider(),
                        product.externalEtfId(),
                        product.ticker(),
                        product.etfName(),
                        product.description(),
                        product.market(),
                        product.currency(),
                        product.riskGrade(),
                        product.isFractionalAvailable(),
                        product.isTradeAvailable(),
                        product.displayOrder(),
                        lastSyncedAt
                ));

        investChnEtfProductRepository.save(chnEtfProduct);
    }
}