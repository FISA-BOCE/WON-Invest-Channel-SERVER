package com.woorifisa.won_invest_channel_server.domain.sweep.service;

import com.woorifisa.won_invest_channel_server.domain.sweep.model.InvestChnOutboxEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepOutboxPublishStatus;
import com.woorifisa.won_invest_channel_server.domain.sweep.repository.InvestChnOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SweepOutboxClaimService {

    private final InvestChnOutboxEventRepository outboxRepository;

    @Transactional
    public List<Long> claimPublishTargets(int batchSize) {
        List<InvestChnOutboxEvent> targets = outboxRepository.findPublishTargets(
                List.of(SweepOutboxPublishStatus.PENDING, SweepOutboxPublishStatus.RETRY),
                LocalDateTime.now(),
                PageRequest.of(0, batchSize)
        );

        targets.forEach(InvestChnOutboxEvent::markProcessing);

        return targets.stream()
                .map(InvestChnOutboxEvent::getOutboxEventId)
                .toList();
    }
}
