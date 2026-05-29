package com.woorifisa.won_invest_channel_server.domain.sweep.service;

import com.woorifisa.won_invest_channel_server.domain.sweep.dto.command.InboxClaimResult;
import com.woorifisa.won_invest_channel_server.domain.sweep.dto.event.SweepRequestedEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.InvestChnInboxEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepEventType;
import com.woorifisa.won_invest_channel_server.domain.sweep.repository.InvestChnInboxEventRepository;
import com.woorifisa.won_invest_channel_server.global.config.SweepRequestConsumerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SweepInboxService {

    private static final String SOURCE_SYSTEM = "CARD_CHANNEL";

    private final InvestChnInboxEventRepository inboxRepository;
    private final SweepRequestConsumerProperties properties;

    @Transactional
    public InboxClaimResult claim(SweepRequestedEvent event, String payload) {
        return inboxRepository.findByIdempotencyKey(event.idempotencyKey())
                .map(this::claimExisting)
                .orElseGet(() -> createAndClaim(event, payload));
    }

    private InboxClaimResult claimExisting(InvestChnInboxEvent inbox) {
        if (inbox.isProcessed()) {
            return InboxClaimResult.alreadyProcessed(inbox.getInboxEventId());
        }

        if (inbox.canRetry(properties.processingTimeoutSeconds())) {
            inbox.markProcessing();
            return InboxClaimResult.claimed(inbox.getInboxEventId());
        }

        return InboxClaimResult.inProgress(inbox.getInboxEventId());
    }

    private InboxClaimResult createAndClaim(SweepRequestedEvent event, String payload) {
        try {
            InvestChnInboxEvent inbox = InvestChnInboxEvent.received(
                    SOURCE_SYSTEM,
                    event.eventId(),
                    SweepEventType.valueOf(event.eventType()),
                    event.sweepRequestId(),
                    payload,
                    event.correlationId(),
                    event.idempotencyKey()
            );
            InvestChnInboxEvent saved = inboxRepository.save(inbox);
            saved.markProcessing();
            return InboxClaimResult.claimed(saved.getInboxEventId());
        } catch (DataIntegrityViolationException e) {
            InvestChnInboxEvent existing = inboxRepository.findByIdempotencyKey(event.idempotencyKey())
                    .orElseThrow(() -> e);
            return claimExisting(existing);
        }
    }

    @Transactional
    public void markProcessed(Long inboxEventId) {
        inboxRepository.findById(inboxEventId)
                .ifPresent(InvestChnInboxEvent::markProcessed);
    }

    @Transactional
    public void markFailed(Long inboxEventId, String errorMessage) {
        inboxRepository.findById(inboxEventId)
                .ifPresent(inbox -> inbox.markFailed(errorMessage));
    }
}
