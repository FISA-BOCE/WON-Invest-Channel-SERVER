package com.woorifisa.won_invest_channel_server.domain.etf.repository;

import com.woorifisa.won_invest_channel_server.domain.etf.model.InvestChnEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfRiskGrade;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


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

    @Query("""
            select product
            from InvestChnEtfProduct product
            where product.isTradeAvailable = true
              and product.isFractionalAvailable = true
              and (:keyword is null
                   or lower(product.ticker) like lower(concat('%', :keyword, '%'))
                   or lower(product.etfName) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(product.description, '')) like lower(concat('%', :keyword, '%')))
              and (:market is null or lower(product.market) = lower(:market))
              and product.currency = :currency
              and (:riskGrade is null or product.riskGrade = :riskGrade)
            order by
              case when product.displayOrder is null then 1 else 0 end,
              product.displayOrder asc,
              product.etfId asc
            """)
    List<InvestChnEtfProduct> findProvidedEtfProducts(
            @Param("keyword") String keyword,
            @Param("market") String market,
            @Param("currency") EtfCurrency currency,
            @Param("riskGrade") EtfRiskGrade riskGrade
    );
}
