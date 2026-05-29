package com.woorifisa.won_invest_channel_server.domain.sweep.model;

import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepEventType;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepInboxProcessStatus;
import com.woorifisa.won_invest_channel_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "invest_chn_inbox_event",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_invest_chn_inbox_idempotency_key", columnNames = "idempotency_key"),
                @UniqueConstraint(name = "uk_invest_chn_inbox_source_event", columnNames = {"source_system", "source_event_id"})
        },
        indexes = {
                @Index(name = "idx_invest_chn_inbox_status_received", columnList = "process_status, received_at"),
                @Index(name = "idx_invest_chn_inbox_correlation_id", columnList = "correlation_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestChnInboxEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inbox_event_id")
    private Long inboxEventId;

    @Column(name = "source_system", nullable = false, length = 30)
    private String sourceSystem;

    @Column(name = "source_event_id", nullable = false, length = 100)
    private String sourceEventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private SweepEventType eventType;

    @Column(name = "sweep_request_id", nullable = false)
    private Long sweepRequestId;

    @Column(name = "payload", nullable = false, columnDefinition = "json")
    private String payload;

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_status", nullable = false, length = 30)
    private SweepInboxProcessStatus processStatus;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "last_error_message", length = 500)
    private String lastErrorMessage;

    private InvestChnInboxEvent(String sourceSystem, String sourceEventId, SweepEventType eventType,
                                Long sweepRequestId, String payload, String correlationId, String idempotencyKey) {
        this.sourceSystem = sourceSystem;
        this.sourceEventId = sourceEventId;
        this.eventType = eventType;
        this.sweepRequestId = sweepRequestId;
        this.payload = payload;
        this.correlationId = correlationId;
        this.idempotencyKey = idempotencyKey;
        this.processStatus = SweepInboxProcessStatus.RECEIVED;
        this.retryCount = 0;
        this.receivedAt = LocalDateTime.now();
    }

    public static InvestChnInboxEvent received(String sourceSystem, String sourceEventId, SweepEventType eventType,
                                               Long sweepRequestId, String payload, String correlationId,
                                               String idempotencyKey) {
        return new InvestChnInboxEvent(sourceSystem, sourceEventId, eventType, sweepRequestId,
                payload, correlationId, idempotencyKey);
    }

    public void markProcessing() {
        this.processStatus = SweepInboxProcessStatus.PROCESSING;
        this.lastErrorMessage = null;
    }

    public void markProcessed() {
        this.processStatus = SweepInboxProcessStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
        this.lastErrorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.processStatus = SweepInboxProcessStatus.FAILED;
        this.retryCount++;
        this.lastErrorMessage = truncate(errorMessage);
    }

    public boolean isProcessed() {
        return this.processStatus == SweepInboxProcessStatus.PROCESSED;
    }

    public boolean isProcessingTimedOut(long timeoutSeconds) {
        return this.processStatus == SweepInboxProcessStatus.PROCESSING
                && getUpdatedAt() != null
                && getUpdatedAt().isBefore(LocalDateTime.now().minusSeconds(timeoutSeconds));
    }

    public boolean canRetry(long timeoutSeconds) {
        return this.processStatus == SweepInboxProcessStatus.FAILED || isProcessingTimedOut(timeoutSeconds);
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 500 ? message : message.substring(0, 500);
    }
}
