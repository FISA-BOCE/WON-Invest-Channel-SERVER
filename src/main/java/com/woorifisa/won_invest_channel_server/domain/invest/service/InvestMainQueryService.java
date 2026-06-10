package com.woorifisa.won_invest_channel_server.domain.invest.service;

import com.woorifisa.won_invest_channel_server.domain.account.model.AccountStatus;
import com.woorifisa.won_invest_channel_server.domain.account.model.InvestChnAccountSummary;
import com.woorifisa.won_invest_channel_server.domain.account.repository.InvestChnAccountSummaryRepository;
import com.woorifisa.won_invest_channel_server.domain.aidb.model.InvestChnAiSummary;
import com.woorifisa.won_invest_channel_server.domain.aidb.repository.InvestChnAiSummaryRepository;
import com.woorifisa.won_invest_channel_server.domain.holding.model.InvestChnEtfHoldingSummary;
import com.woorifisa.won_invest_channel_server.domain.holding.repository.InvestChnEtfHoldingSummaryRepository;
import com.woorifisa.won_invest_channel_server.domain.invest.dto.response.InvestMainResponse;
import com.woorifisa.won_invest_channel_server.domain.invest.exception.code.InvestErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestMainQueryService {

    private static final String DEFAULT_ACCOUNT_HOLDER_NAME = "고객님";
    private static final String DEFAULT_KRW_STATUS = "전액 환전·매수 완료";

    private final InvestChnAccountSummaryRepository accountSummaryRepository;
    private final InvestChnAiSummaryRepository aiSummaryRepository;
    private final InvestChnEtfHoldingSummaryRepository holdingSummaryRepository;

    public InvestMainResponse getInvestMain(UUID userUuid) {
        InvestChnAccountSummary accountSummary = findRepresentativeAccount(userUuid);
        InvestChnAiSummary aiSummary = aiSummaryRepository
                .findTopByUserUuidAndInvestAccountUuidOrderByLastSyncedAtDescSummaryIdDesc(
                        userUuid,
                        accountSummary.getInvestAccountUuid()
                )
                .orElse(null);

        List<InvestMainResponse.RecentPayment> recentPayments = holdingSummaryRepository
                .findTop2ByInvestAccountUuidOrderByLastSyncedAtDescHoldingSummaryIdDesc(
                        accountSummary.getInvestAccountUuid()
                )
                .stream()
                .map(this::toRecentPayment)
                .toList();

        return new InvestMainResponse(
                amountOrZero(aiSummary == null ? null : aiSummary.getEvaluationAmount()),
                amountOrZero(aiSummary == null ? null : aiSummary.getProfitLossAmount()),
                amountOrZero(aiSummary == null ? null : aiSummary.getProfitLossRate()),
                new InvestMainResponse.Account(
                        accountSummary.getInvestAccountUuid(),
                        accountSummary.getAccountNoDisplay(),
                        maskAccountHolderName(resolveAccountHolderName(accountSummary))
                ),
                new InvestMainResponse.CashBalance(
                        BigDecimal.ZERO,
                        DEFAULT_KRW_STATUS,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ),
                recentPayments
        );
    }

    private InvestChnAccountSummary findRepresentativeAccount(UUID userUuid) {
        List<InvestChnAccountSummary> activeAccounts =
                accountSummaryRepository.findAllByUserUuidAndAccountStatusOrderByCreatedAtDesc(userUuid, AccountStatus.ACTIVE);
        if (!activeAccounts.isEmpty()) {
            return activeAccounts.get(0);
        }

        return accountSummaryRepository.findAllByUserUuidOrderByCreatedAtDesc(userUuid)
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(InvestErrorCode.ACCOUNT_NOT_FOUND));
    }

    private InvestMainResponse.RecentPayment toRecentPayment(InvestChnEtfHoldingSummary holdingSummary) {
        return new InvestMainResponse.RecentPayment(
                holdingSummary.getEtfName(),
                holdingSummary.getTicker(),
                amountOrZero(holdingSummary.getHoldingQuantity()),
                amountOrZero(holdingSummary.getEvaluationAmount())
        );
    }

    private BigDecimal amountOrZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String resolveAccountHolderName(InvestChnAccountSummary accountSummary) {
        String accountHolderName = accountSummary.getAccountHolderName();
        if (accountHolderName == null || accountHolderName.isBlank()) {
            return DEFAULT_ACCOUNT_HOLDER_NAME;
        }
        return accountHolderName;
    }

    static String maskAccountHolderName(String accountHolderName) {
        if (accountHolderName == null || accountHolderName.isBlank()) {
            return accountHolderName;
        }

        int length = accountHolderName.length();
        if (length == 3) {
            return accountHolderName.charAt(0) + "*" + accountHolderName.substring(2);
        }
        if (length == 4) {
            return accountHolderName.charAt(0) + "**" + accountHolderName.substring(3);
        }
        return accountHolderName;
    }
}
