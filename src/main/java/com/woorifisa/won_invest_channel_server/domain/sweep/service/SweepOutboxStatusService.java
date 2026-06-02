package com.woorifisa.won_invest_channel_server.domain.sweep.service;

import com.woorifisa.won_invest_channel_server.domain.sweep.dto.command.SweepOutboxPublishMessage;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.InvestChnOutboxEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.repository.InvestChnOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SweepOutboxStatusService {

    private final InvestChnOutboxEventRepository outboxRepository;

    @Transactional(readOnly = true)
    public SweepOutboxPublishMessage getPublishMessage(Long outboxEventId) {
        InvestChnOutboxEvent outbox = outboxRepository.findById(outboxEventId)
                .orElseThrow(() -> new IllegalStateException("스윕 결과 outbox를 찾을 수 없습니다."));

        if (!outbox.isProcessing()) {
            throw new IllegalStateException("스윕 결과 outbox가 발행 가능한 상태가 아닙니다.");
        }

        return new SweepOutboxPublishMessage(
                outbox.getOutboxEventId(),
                outbox.getEventId(),
                outbox.getEventType(),
                outbox.getSweepRequestId(),
                outbox.getCorrelationId(),
                outbox.getIdempotencyKey(),
                outbox.getPayload()
        );
    }

    @Transactional
    public void markPublished(Long outboxEventId) {
        outboxRepository.findById(outboxEventId)
                .filter(InvestChnOutboxEvent::isProcessing)
                .ifPresent(InvestChnOutboxEvent::markPublished);
    }

    @Transactional
    public void markPublishFailed(Long outboxEventId, String errorMessage, int maxRetryCount) {
        outboxRepository.findById(outboxEventId)
                .filter(InvestChnOutboxEvent::isProcessing)
                .ifPresent(outbox -> outbox.markPublishFailed(errorMessage, maxRetryCount));
    }
}
