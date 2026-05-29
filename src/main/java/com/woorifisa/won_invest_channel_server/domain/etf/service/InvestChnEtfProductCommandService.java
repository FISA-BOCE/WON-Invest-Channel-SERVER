package com.woorifisa.won_invest_channel_server.domain.etf.service;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.ExternalEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.model.InvestChnEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.repository.InvestChnEtfProductRepository;
import com.woorifisa.won_invest_channel_server.domain.etf.exception.code.EtfErrorCode;
import com.woorifisa.won_invest_channel_server.domain.etf.exception.EtfSyncException;
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
            throw new EtfSyncException(EtfErrorCode.CORE_ETF_ID_EMPTY);
        }

        if (product == null) {
            throw new EtfSyncException(EtfErrorCode.ETF_PRODUCT_EMPTY);
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
