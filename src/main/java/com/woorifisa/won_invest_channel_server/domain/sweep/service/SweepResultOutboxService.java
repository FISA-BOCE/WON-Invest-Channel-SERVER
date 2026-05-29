package com.woorifisa.won_invest_channel_server.domain.sweep.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorifisa.won_invest_channel_server.domain.sweep.dto.event.SweepRequestedEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.dto.response.InvestCoreSweepExecutionResponse;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.InvestChnOutboxEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepEventType;
import com.woorifisa.won_invest_channel_server.domain.sweep.repository.InvestChnOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SweepResultOutboxService {

    private final InvestChnOutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveCompleted(SweepRequestedEvent event, InvestCoreSweepExecutionResponse response) {
        saveResult(event, response.sweepExecutionId(), SweepEventType.SWEEP_INVESTMENT_COMPLETED, null, null);
    }

    @Transactional
    public void saveFailed(SweepRequestedEvent event, Long sweepExecutionId, String failureCode, String failureMessage) {
        saveResult(event, sweepExecutionId, SweepEventType.SWEEP_INVESTMENT_FAILED, failureCode, failureMessage);
    }

    private void saveResult(SweepRequestedEvent event, Long sweepExecutionId, SweepEventType eventType,
                            String failureCode, String failureMessage) {
        if (outboxRepository.findByIdempotencyKeyAndEventType(event.idempotencyKey(), eventType).isPresent()) {
            return;
        }

        String eventId = "INVEST-SWEEP-" + UUID.randomUUID();
        String payload = toPayload(eventId, event, sweepExecutionId, eventType, failureCode, failureMessage);

        InvestChnOutboxEvent outbox = InvestChnOutboxEvent.pending(
                eventId,
                eventType,
                event.sweepRequestId(),
                sweepExecutionId,
                payload,
                event.correlationId(),
                event.idempotencyKey()
        );

        outboxRepository.save(outbox);
    }

    private String toPayload(
            String eventId,
            SweepRequestedEvent event,
            Long sweepExecutionId,
            SweepEventType eventType,
            String failureCode,
            String failureMessage
    ) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventId", eventId);
            payload.put("eventType", eventType.name());
            payload.put("correlationId", event.correlationId());
            payload.put("idempotencyKey", event.idempotencyKey());
            payload.put("sweepRequestId", event.sweepRequestId());
            payload.put("sweepExecutionId", sweepExecutionId);
            payload.put("pointLedgerId", event.pointLedgerId());
            payload.put("userUuid", event.userUuid());
            payload.put("cardUserUuid", event.cardUserUuid());
            payload.put("baseMonth", event.baseMonth());
            payload.put("pointAmount", event.pointAmount());
            payload.put("krwAmount", event.krwAmount());
            payload.put("etfId", event.etfId());
            payload.put("failureCode", failureCode);
            payload.put("failureMessage", truncate(failureMessage));

            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("스윕 결과 이벤트 payload 생성에 실패했습니다.", e);
        }
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 500 ? message : message.substring(0, 500);
    }

}
