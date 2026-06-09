package com.woorifisa.won_invest_channel_server.domain.holding.model;

import com.woorifisa.won_invest_channel_server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@SuppressWarnings("PMD.RedundantFieldInitializer")
@Table(
        name = "invest_chn_etf_holding_summary",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_invest_chn_holding_account_etf",
                        columnNames = {"invest_account_uuid", "etf_id"}
                )
        },
        indexes = {
                @Index(name = "idx_invest_chn_holding_user_uuid", columnList = "user_uuid"),
                @Index(name = "idx_invest_chn_holding_invest_user_uuid", columnList = "invest_user_uuid"),
                @Index(name = "idx_invest_chn_holding_invest_account_uuid", columnList = "invest_account_uuid"),
                @Index(name = "idx_invest_chn_holding_etf_id", columnList = "etf_id"),
                @Index(name = "idx_invest_chn_holding_ticker", columnList = "ticker")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestChnEtfHoldingSummary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_summary_id", nullable = false)
    private Long holdingSummaryId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "user_uuid", nullable = false, length = 36)
    private UUID userUuid;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "invest_user_uuid", nullable = false, length = 36)
    private UUID investUserUuid;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "invest_account_uuid", nullable = false, length = 36)
    private UUID investAccountUuid;

    @Column(name = "etf_id", nullable = false)
    private Long etfId;

    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker;

    @Column(name = "etf_name", nullable = false, length = 100)
    private String etfName;

    // JPA 생성 경로에서도 null-safe 기본값을 유지하고, builder에서는 defaultZero(...)로 명시적으로 덮어쓴다.
    @Column(name = "holding_quantity", nullable = false, precision = 18, scale = 8)
    private BigDecimal holdingQuantity = BigDecimal.ZERO;

    @Column(name = "average_buy_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal averageBuyPrice = BigDecimal.ZERO;

    @Column(name = "evaluation_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal evaluationAmount = BigDecimal.ZERO;

    @Column(name = "profit_loss_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal profitLossAmount = BigDecimal.ZERO;

    @Column(name = "profit_loss_rate", nullable = false, precision = 10, scale = 6)
    private BigDecimal profitLossRate = BigDecimal.ZERO;

    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;

    @Builder
    public InvestChnEtfHoldingSummary(
            UUID userUuid,
            UUID investUserUuid,
            UUID investAccountUuid,
            Long etfId,
            String ticker,
            String etfName,
            BigDecimal holdingQuantity,
            BigDecimal averageBuyPrice,
            BigDecimal evaluationAmount,
            BigDecimal profitLossAmount,
            BigDecimal profitLossRate,
            LocalDateTime lastSyncedAt
    ) {
        this.userUuid = userUuid;
        this.investUserUuid = investUserUuid;
        this.investAccountUuid = investAccountUuid;
        this.etfId = etfId;
        this.ticker = ticker;
        this.etfName = etfName;
        this.holdingQuantity = defaultZero(holdingQuantity);
        this.averageBuyPrice = defaultZero(averageBuyPrice);
        this.evaluationAmount = defaultZero(evaluationAmount);
        this.profitLossAmount = defaultZero(profitLossAmount);
        this.profitLossRate = defaultZero(profitLossRate);
        this.lastSyncedAt = lastSyncedAt;
    }

    private static BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
