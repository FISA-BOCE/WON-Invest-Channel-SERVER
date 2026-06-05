package com.woorifisa.won_invest_channel_server.domain.aidb.repository;

import com.woorifisa.won_invest_channel_server.domain.aidb.dto.response.AiDbHoldingListResponse;
import com.woorifisa.won_invest_channel_server.domain.aidb.dto.response.AiDbHoldingSummaryResponse;
import com.woorifisa.won_invest_channel_server.domain.aidb.model.InvestChnAiSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestChnAiSummaryRepository extends JpaRepository<InvestChnAiSummary, Long> {

    @Query(
            value = """
                    SELECT
                        h.user_uuid AS userUuid,
                        h.invest_account_uuid AS investAccountUuid,
                        h.etf_id AS etfId,
                        h.ticker AS ticker,
                        h.etf_name AS etfName,
                        h.holding_quantity AS holdingQuantity,
                        h.evaluation_amount AS evaluationAmount,
                        h.last_synced_at AS lastSyncedAt
                    FROM invest_chn_etf_holding_summary h
                    WHERE h.user_uuid = :userUuid
                    ORDER BY h.etf_id
                    """,
            nativeQuery = true
    )
    List<HoldingListRow> findHoldingListRowsByUserUuid(@Param("userUuid") String userUuid);

    @Query(
            value = """
                    SELECT
                        s.user_uuid AS userUuid,
                        s.invest_account_uuid AS investAccountUuid,
                        s.total_buy_amount AS totalBuyAmount,
                        s.evaluation_amount AS totalEvaluationAmount,
                        s.profit_loss_amount AS totalProfitLossAmount,
                        s.profit_loss_rate AS totalProfitLossRate,
                        s.last_synced_at AS summaryLastSyncedAt,
                        h.etf_id AS etfId,
                        h.ticker AS ticker,
                        h.etf_name AS etfName,
                        h.holding_quantity AS holdingQuantity,
                        h.average_buy_price AS averageBuyPrice,
                        h.evaluation_amount AS evaluationAmount,
                        h.profit_loss_amount AS profitLossAmount,
                        h.profit_loss_rate AS profitLossRate,
                        h.last_synced_at AS holdingLastSyncedAt
                    FROM invest_chn_ai_summary s
                    LEFT JOIN invest_chn_etf_holding_summary h
                        ON h.user_uuid COLLATE utf8mb4_0900_ai_ci = s.user_uuid COLLATE utf8mb4_0900_ai_ci
                        AND h.invest_account_uuid COLLATE utf8mb4_0900_ai_ci = s.invest_account_uuid COLLATE utf8mb4_0900_ai_ci
                    WHERE s.user_uuid = :userUuid
                      AND s.summary_id = (
                          SELECT s2.summary_id
                          FROM invest_chn_ai_summary s2
                          WHERE s2.user_uuid = :userUuid
                          ORDER BY s2.last_synced_at DESC, s2.summary_id DESC
                          LIMIT 1
                      )
                    ORDER BY h.etf_id
                    """,
            nativeQuery = true
    )
    List<HoldingSummaryRow> findHoldingSummaryRowsByUserUuid(@Param("userUuid") String userUuid);

    default Optional<AiDbHoldingListResponse> findHoldingListResponseByUserUuid(UUID userUuid) {
        List<HoldingListRow> rows = findHoldingListRowsByUserUuid(userUuid.toString());
        if (rows.isEmpty()) {
            return Optional.empty();
        }

        HoldingListRow first = rows.get(0);
        List<AiDbHoldingListResponse.Holding> holdings = rows.stream()
                .map(row -> new AiDbHoldingListResponse.Holding(
                        row.getEtfId(),
                        row.getTicker(),
                        row.getEtfName(),
                        row.getHoldingQuantity(),
                        row.getEvaluationAmount(),
                        row.getLastSyncedAt()
                ))
                .toList();

        return Optional.of(new AiDbHoldingListResponse(
                first.getUserUuid(),
                first.getInvestAccountUuid(),
                holdings.size(),
                holdings
        ));
    }

    default Optional<AiDbHoldingSummaryResponse> findHoldingSummaryResponseByUserUuid(UUID userUuid) {
        List<HoldingSummaryRow> rows = findHoldingSummaryRowsByUserUuid(userUuid.toString());
        if (rows.isEmpty()) {
            return Optional.empty();
        }

        HoldingSummaryRow first = rows.get(0);
        List<AiDbHoldingSummaryResponse.Holding> holdings = rows.stream()
                .filter(row -> row.getEtfId() != null)
                .map(row -> new AiDbHoldingSummaryResponse.Holding(
                        row.getEtfId(),
                        row.getTicker(),
                        row.getEtfName(),
                        row.getHoldingQuantity(),
                        row.getAverageBuyPrice(),
                        row.getEvaluationAmount(),
                        row.getProfitLossAmount(),
                        row.getProfitLossRate(),
                        row.getHoldingLastSyncedAt()
                ))
                .toList();

        return Optional.of(new AiDbHoldingSummaryResponse(
                first.getUserUuid(),
                first.getInvestAccountUuid(),
                first.getTotalBuyAmount(),
                first.getTotalEvaluationAmount(),
                first.getTotalProfitLossAmount(),
                first.getTotalProfitLossRate(),
                holdings.size(),
                first.getSummaryLastSyncedAt(),
                holdings
        ));
    }

    interface HoldingListRow {
        UUID getUserUuid();

        UUID getInvestAccountUuid();

        Long getEtfId();

        String getTicker();

        String getEtfName();

        BigDecimal getHoldingQuantity();

        BigDecimal getEvaluationAmount();

        LocalDateTime getLastSyncedAt();
    }

    interface HoldingSummaryRow {
        UUID getUserUuid();

        UUID getInvestAccountUuid();

        BigDecimal getTotalBuyAmount();

        BigDecimal getTotalEvaluationAmount();

        BigDecimal getTotalProfitLossAmount();

        BigDecimal getTotalProfitLossRate();

        LocalDateTime getSummaryLastSyncedAt();

        Long getEtfId();

        String getTicker();

        String getEtfName();

        BigDecimal getHoldingQuantity();

        BigDecimal getAverageBuyPrice();

        BigDecimal getEvaluationAmount();

        BigDecimal getProfitLossAmount();

        BigDecimal getProfitLossRate();

        LocalDateTime getHoldingLastSyncedAt();
    }
}
