package com.woorifisa.won_invest_channel_server.domain.holding.repository;

import com.woorifisa.won_invest_channel_server.domain.holding.model.InvestChnEtfHoldingSummary;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestChnEtfHoldingSummaryRepository extends JpaRepository<InvestChnEtfHoldingSummary, Long> {

    List<InvestChnEtfHoldingSummary> findByInvestAccountUuidOrderByLastSyncedAtDescHoldingSummaryIdDesc(UUID investAccountUuid);
}
