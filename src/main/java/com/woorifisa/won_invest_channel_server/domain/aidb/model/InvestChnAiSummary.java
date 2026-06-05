package com.woorifisa.won_invest_channel_server.domain.aidb.model;

import com.woorifisa.won_invest_channel_server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
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
@Table(
        name = "invest_chn_ai_summary",
        indexes = {
                @Index(name = "idx_invest_chn_ai_summary_user_uuid", columnList = "user_uuid"),
                @Index(name = "idx_invest_chn_ai_summary_invest_account_uuid", columnList = "invest_account_uuid")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestChnAiSummary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id", nullable = false)
    private Long summaryId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "user_uuid", nullable = false, length = 36)
    private UUID userUuid;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "invest_account_uuid", nullable = false, length = 36)
    private UUID investAccountUuid;

    @Column(name = "total_buy_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal totalBuyAmount = BigDecimal.ZERO;

    @Column(name = "evaluation_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal evaluationAmount = BigDecimal.ZERO;

    @Column(name = "profit_loss_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal profitLossAmount = BigDecimal.ZERO;

    @Column(name = "profit_loss_rate", nullable = false, precision = 10, scale = 6)
    private BigDecimal profitLossRate = BigDecimal.ZERO;

    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;

    @Builder
    public InvestChnAiSummary(
            UUID userUuid,
            UUID investAccountUuid,
            BigDecimal totalBuyAmount,
            BigDecimal evaluationAmount,
            BigDecimal profitLossAmount,
            BigDecimal profitLossRate,
            LocalDateTime lastSyncedAt
    ) {
        this.userUuid = userUuid;
        this.investAccountUuid = investAccountUuid;
        this.totalBuyAmount = defaultZero(totalBuyAmount);
        this.evaluationAmount = defaultZero(evaluationAmount);
        this.profitLossAmount = defaultZero(profitLossAmount);
        this.profitLossRate = defaultZero(profitLossRate);
        this.lastSyncedAt = lastSyncedAt;
    }

    private static BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
