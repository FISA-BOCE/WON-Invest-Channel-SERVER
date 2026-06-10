package com.woorifisa.won_invest_channel_server.domain.invest.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record InvestMainResponse(
        BigDecimal totalEvaluationAmount,
        BigDecimal profitLossAmount,
        BigDecimal profitLossRate,
        Account account,
        CashBalance cashBalance,
        List<RecentPayment> recentPayments
) {

    public record Account(
            UUID investAccountUuid,
            String accountNoDisplay,
            String accountHolderName
    ) {
    }

    public record CashBalance(
            BigDecimal krwAmount,
            String krwStatus,
            BigDecimal usdAmount,
            BigDecimal usdKrwAmount
    ) {
    }

    public record RecentPayment(
            String etfName,
            String ticker,
            BigDecimal holdingQuantity,
            BigDecimal evaluationAmount
    ) {
    }
}
