package com.woorifisa.won_invest_channel_server.domain.etf.repository;

import com.woorifisa.won_invest_channel_server.domain.etf.model.InvestChnEtfProduct;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// 기본 upsert 기준 - Core에서 반환받은 etf_id
public interface InvestChnEtfProductRepository extends JpaRepository<InvestChnEtfProduct, Long> {

    // 보조 메서드 - externalProvider, ticker 기준
    Optional<InvestChnEtfProduct> findByExternalProviderAndTicker(
            String externalProvider,
            String ticker
    );

    // 중복 여부 확인 메서드 - externalProvider, ticker 기준
    boolean existsByExternalProviderAndTicker(
            String externalProvider,
            String ticker
    );
}
