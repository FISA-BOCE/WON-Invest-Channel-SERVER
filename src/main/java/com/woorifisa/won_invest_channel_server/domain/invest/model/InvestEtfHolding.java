package com.woorifisa.won_invest_channel_server.domain.invest.model;

import com.woorifisa.won_invest_channel_server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Table(name = "invest_chn_account_etf_holding")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestEtfHolding extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_id")
    private Long id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "account_uuid", nullable = false)
    private UUID accountUuid;

    @Column(name = "etf_id", nullable = false)
    private Long etfId;

    @Column(name = "etf_name", nullable = false)
    private String etfName;

    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker;

    @Column(name = "holding_quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal holdingQuantity;

    @Column(name = "average_buy_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal averageBuyPrice;

    @Column(name = "evaluation_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal evaluationAmount;

    @Column(name = "profit_loss_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal profitLossAmount;

    @Column(name = "profit_loss_rate", nullable = false, precision = 9, scale = 2)
    private BigDecimal profitLossRate;

    @Builder
    public InvestEtfHolding(
            UUID accountUuid,
            Long etfId,
            String etfName,
            String ticker,
            BigDecimal holdingQuantity,
            BigDecimal averageBuyPrice,
            BigDecimal evaluationAmount,
            BigDecimal profitLossAmount,
            BigDecimal profitLossRate
    ) {
        this.accountUuid = accountUuid;
        this.etfId = etfId;
        this.etfName = etfName;
        this.ticker = ticker;
        this.holdingQuantity = holdingQuantity;
        this.averageBuyPrice = averageBuyPrice;
        this.evaluationAmount = evaluationAmount;
        this.profitLossAmount = profitLossAmount;
        this.profitLossRate = profitLossRate;
    }
}
