// WON해요에서 실제로 제공할 ETF만 올리는 선별 목록

package com.woorifisa.won_invest_channel_server.domain.etf.model;

import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfRiskGrade;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
@Table(name = "invest_chn_etf_product",
        indexes = {
            @Index(
                name = "idx_invest_chn_etf_product_list",
                columnList = "currency, is_trade_available, is_fractional_available, display_order, etf_id"
            )
}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestChnEtfProduct {

    @Id
    @Column(name = "etf_id", nullable = false)
    private Long etfId;

    // 외부 API 제공자 - KIS
    @Column(name = "external_provider", nullable = false, length = 50)
    private String externalProvider;

    // 외부 API에서 쓰는 ETF ID
    @Column(name = "external_etf_id", length = 100)
    private String externalEtfId;

    // VOO, QQQ, SCHD, SPY 등
    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker;

    //ex) Vanguard S&P 500 ETF, Invesco QQQ Trust 등
    @Column(name = "etf_name", nullable = false, length = 100)
    private String etfName;

    // 앱 화면에 보여줄 ETF 설명
    // '미국 대표 대형주에 분산 투자하는 ETF입니다.', '기술주 중심의 나스닥 100 지수를 추종합니다.', '배당 성향이 높은 미국 기업에 투자합니다.' 등
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    //거래 시장 - ex) NYSE, NASDAQ, AMEX 등
    @Column(name = "market", length = 30)
    private String market;

    // ETF 거래 통화 - USD, KRW, CAD, EUR +a 추가 가능
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 10)
    private EtfCurrency currency;

    // 위험등급
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_grade", length = 30)
    private EtfRiskGrade riskGrade;

    // 소수점 매수 가능 여부
    @Column(name = "is_fractional_available", nullable = false)
    private Boolean isFractionalAvailable;

    // 현재 이 ETF가 거래 가능한지
    @Column(name = "is_trade_available", nullable = false)
    private Boolean isTradeAvailable;

    // 앱 화면에 ETF 목록을 보여줄 때 순서를 정하는 값
    @Column(name = "display_order")
    private Integer displayOrder;

    // 마지막으로 동기화된 시간
    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;


    public InvestChnEtfProduct(
            Long etfId,
            String externalProvider,
            String externalEtfId,
            String ticker,
            String etfName,
            String description,
            String market,
            EtfCurrency currency,
            EtfRiskGrade riskGrade,
            Boolean isFractionalAvailable,
            Boolean isTradeAvailable,
            Integer displayOrder,
            LocalDateTime lastSyncedAt
    ) {
        this.etfId = etfId;
        this.externalProvider = externalProvider;
        this.externalEtfId = externalEtfId;
        this.ticker = ticker;
        this.etfName = etfName;
        this.description = description;
        this.market = market;
        this.currency = currency;
        this.riskGrade = riskGrade;
        this.isFractionalAvailable = isFractionalAvailable;
        this.isTradeAvailable = isTradeAvailable;
        this.displayOrder = displayOrder;
        this.lastSyncedAt = Objects.requireNonNull(lastSyncedAt, "lastSyncedAt must not be null");;
    }

    // 신규 ETF 상품을 Channel 테이블에 처음 저장할 때
    public static InvestChnEtfProduct create(
            Long etfId,
            String externalProvider,
            String externalEtfId,
            String ticker,
            String etfName,
            String description,
            String market,
            EtfCurrency currency,
            EtfRiskGrade riskGrade,
            Boolean isFractionalAvailable,
            Boolean isTradeAvailable,
            Integer displayOrder,
            LocalDateTime lastSyncedAt
    ) {
        return new InvestChnEtfProduct(
                etfId,
                externalProvider,
                externalEtfId,
                ticker,
                etfName,
                description,
                market,
                currency,
                riskGrade,
                isFractionalAvailable,
                isTradeAvailable,
                displayOrder,
                lastSyncedAt
        );
    }

    // 이미 존재하는 ETF를 다시 동기화할 때
    public void updateProductInfo(
            String externalProvider,
            String externalEtfId,
            String ticker,
            String etfName,
            String description,
            String market,
            EtfCurrency currency,
            EtfRiskGrade riskGrade,
            Boolean isFractionalAvailable,
            Boolean isTradeAvailable,
            LocalDateTime lastSyncedAt
    ) {
        this.externalProvider = externalProvider;
        this.externalEtfId = externalEtfId;
        this.ticker = ticker;
        this.etfName = etfName;
        this.description = description;
        this.market = market;
        this.currency = currency;
        this.riskGrade = riskGrade;
        this.isFractionalAvailable = isFractionalAvailable;
        this.isTradeAvailable = isTradeAvailable;
        this.lastSyncedAt = Objects.requireNonNull(lastSyncedAt, "lastSyncedAt must not be null");
    }

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
