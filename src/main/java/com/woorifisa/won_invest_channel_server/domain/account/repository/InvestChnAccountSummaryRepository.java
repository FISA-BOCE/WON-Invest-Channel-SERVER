package com.woorifisa.won_invest_channel_server.domain.account.repository;

import com.woorifisa.won_invest_channel_server.domain.account.model.InvestChnAccountSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvestChnAccountSummaryRepository extends JpaRepository<InvestChnAccountSummary, Long> {
    Optional<InvestChnAccountSummary> findByInvestAccountUuid(UUID investAccountUuid);
}
