package com.woorifisa.won_invest_channel_server.domain.sweep.model;

import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepEventType;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepOutboxPublishStatus;
import com.woorifisa.won_invest_channel_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "invest_chn_outbox_event",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_invest_chn_outbox_idempotency_event", columnNames = {"idempotency_key"})
        },
        indexes = {
                @Index(name = "idx_invest_chn_outbox_status_retry", columnList = "publish_status, next_retry_at"),
                @Index(name = "idx_invest_chn_outbox_correlation_id", columnList = "correlation_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestChnOutboxEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbox_event_id")
    private Long outboxEventId;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private SweepEventType eventType;

    @Column(name = "sweep_request_id", nullable = false)
    private Long sweepRequestId;

    @Column(name = "sweep_execution_id")
    private Long sweepExecutionId;

    @Column(name = "payload", nullable = false, columnDefinition = "json")
    private String payload;

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "publish_status", nullable = false, length = 30)
    private SweepOutboxPublishStatus publishStatus;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "last_error_message", length = 500)
    private String lastErrorMessage;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    private InvestChnOutboxEvent(String eventId, SweepEventType eventType, Long sweepRequestId,
                                 Long sweepExecutionId, String payload, String correlationId,
                                 String idempotencyKey) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.sweepRequestId = sweepRequestId;
        this.sweepExecutionId = sweepExecutionId;
        this.payload = payload;
        this.correlationId = correlationId;
        this.idempotencyKey = idempotencyKey;
        this.publishStatus = SweepOutboxPublishStatus.PENDING;
        this.retryCount = 0;
    }

    public static InvestChnOutboxEvent pending(String eventId, SweepEventType eventType, Long sweepRequestId,
                                               Long sweepExecutionId, String payload, String correlationId,
                                               String idempotencyKey) {
        return new InvestChnOutboxEvent(eventId, eventType, sweepRequestId, sweepExecutionId,
                payload, correlationId, idempotencyKey);
    }

}
