package com.woorifisa.won_invest_channel_server.domain.sweep.service;

import com.woorifisa.won_invest_channel_server.domain.sweep.dto.command.SweepOutboxPublishMessage;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.InvestChnOutboxEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepEventType;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepOutboxPublishStatus;
import com.woorifisa.won_invest_channel_server.domain.sweep.repository.InvestChnOutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SweepOutboxStatusServiceTest {

    private InvestChnOutboxEventRepository outboxRepository;
    private SweepOutboxStatusService statusService;

    @BeforeEach
    void setUp() {
        outboxRepository = mock(InvestChnOutboxEventRepository.class);
        statusService = new SweepOutboxStatusService(outboxRepository);
    }

    @Test
    @DisplayName("PROCESSING 상태이면 발행 메시지를 반환한다")
    void getPublishMessage() {
        InvestChnOutboxEvent outbox = createOutbox();
        outbox.markProcessing();

        when(outboxRepository.findById(1L)).thenReturn(Optional.of(outbox));

        SweepOutboxPublishMessage message = statusService.getPublishMessage(1L);

        assertThat(message.outboxEventId()).isEqualTo(1L);
        assertThat(message.eventId()).isEqualTo("INVEST-SWEEP-1");
        assertThat(message.eventType()).isEqualTo(SweepEventType.SWEEP_INVESTMENT_COMPLETED);
        assertThat(message.sweepRequestId()).isEqualTo(1L);
        assertThat(message.correlationId()).isEqualTo("CORR-1");
        assertThat(message.idempotencyKey()).isEqualTo("SWEEP:POINT_LEDGER:1");
        assertThat(message.payload()).isEqualTo("{}");
    }

    @Test
    @DisplayName("outbox가 없으면 예외를 던진다")
    void getPublishMessageNotFound() {
        when(outboxRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statusService.getPublishMessage(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("스윕 결과 outbox를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("PROCESSING 상태가 아니면 예외를 던진다")
    void getPublishMessageInvalidState() {
        InvestChnOutboxEvent outbox = createOutbox();

        when(outboxRepository.findById(1L)).thenReturn(Optional.of(outbox));

        assertThatThrownBy(() -> statusService.getPublishMessage(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("스윕 결과 outbox가 발행 가능한 상태가 아닙니다.");
    }

    @Test
    @DisplayName("PROCESSING 상태이면 PUBLISHED로 변경한다")
    void markPublished() {
        InvestChnOutboxEvent outbox = createOutbox();
        outbox.markProcessing();

        when(outboxRepository.findById(1L)).thenReturn(Optional.of(outbox));

        statusService.markPublished(1L);

        assertThat(outbox.getPublishStatus()).isEqualTo(SweepOutboxPublishStatus.PUBLISHED);
        assertThat(outbox.getPublishedAt()).isNotNull();
        assertThat(outbox.getLastErrorMessage()).isNull();
    }

    @Test
    @DisplayName("PROCESSING 상태가 아니면 markPublished를 skip한다")
    void markPublishedSkip() {
        InvestChnOutboxEvent outbox = createOutbox();

        when(outboxRepository.findById(1L)).thenReturn(Optional.of(outbox));

        statusService.markPublished(1L);

        assertThat(outbox.getPublishStatus()).isEqualTo(SweepOutboxPublishStatus.PENDING);
        assertThat(outbox.getPublishedAt()).isNull();
    }

    @Test
    @DisplayName("PROCESSING 상태에서 발행 실패하면 RETRY로 변경한다")
    void markPublishFailedRetry() {
        InvestChnOutboxEvent outbox = createOutbox();
        outbox.markProcessing();

        when(outboxRepository.findById(1L)).thenReturn(Optional.of(outbox));

        statusService.markPublishFailed(1L, "SQS timeout", 3);

        assertThat(outbox.getPublishStatus()).isEqualTo(SweepOutboxPublishStatus.RETRY);
        assertThat(outbox.getRetryCount()).isEqualTo(1);
        assertThat(outbox.getLastErrorMessage()).contains("SQS timeout");
        assertThat(outbox.getNextRetryAt()).isNotNull();
    }

    @Test
    @DisplayName("최대 재시도 횟수에 도달하면 FAILED로 변경한다")
    void markPublishFailedMaxRetry() {
        InvestChnOutboxEvent outbox = createOutbox();
        outbox.markProcessing();
        outbox.markPublishFailed("first", 3);
        outbox.markProcessing();
        outbox.markPublishFailed("second", 3);
        outbox.markProcessing();

        when(outboxRepository.findById(1L)).thenReturn(Optional.of(outbox));

        statusService.markPublishFailed(1L, "third", 3);

        assertThat(outbox.getPublishStatus()).isEqualTo(SweepOutboxPublishStatus.FAILED);
        assertThat(outbox.getRetryCount()).isEqualTo(3);
        assertThat(outbox.getNextRetryAt()).isNull();
    }

    private InvestChnOutboxEvent createOutbox() {
        InvestChnOutboxEvent outbox = InvestChnOutboxEvent.pending(
                "INVEST-SWEEP-1",
                SweepEventType.SWEEP_INVESTMENT_COMPLETED,
                1L,
                10L,
                "{}",
                "CORR-1",
                "SWEEP:POINT_LEDGER:1"
        );
        org.springframework.test.util.ReflectionTestUtils.setField(outbox, "outboxEventId", 1L);
        return outbox;
    }
}
