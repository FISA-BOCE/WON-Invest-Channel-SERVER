package com.woorifisa.won_invest_channel_server.domain.account.repository;

import com.woorifisa.won_invest_channel_server.domain.account.model.AccountStatus;
import com.woorifisa.won_invest_channel_server.domain.account.model.InvestChnAccountSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvestChnAccountSummaryRepository extends JpaRepository<InvestChnAccountSummary, UUID> {

    List<InvestChnAccountSummary> findAllByUserUuidOrderByCreatedAtDesc(UUID userUuid);

    List<InvestChnAccountSummary> findAllByUserUuidAndAccountStatusOrderByCreatedAtDesc(UUID userUuid, AccountStatus accountStatus);
}
