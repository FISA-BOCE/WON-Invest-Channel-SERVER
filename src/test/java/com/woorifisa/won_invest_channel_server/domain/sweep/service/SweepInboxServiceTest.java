package com.woorifisa.won_invest_channel_server.domain.sweep.service;

import com.woorifisa.won_invest_channel_server.domain.sweep.dto.command.InboxClaimResult;
import com.woorifisa.won_invest_channel_server.domain.sweep.dto.event.SweepRequestedEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.InvestChnInboxEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepEventType;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepInboxProcessStatus;
import com.woorifisa.won_invest_channel_server.domain.sweep.repository.InvestChnInboxEventRepository;
import com.woorifisa.won_invest_channel_server.global.config.SweepRequestConsumerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SweepInboxServiceTest {

    private InvestChnInboxEventRepository inboxRepository;
    private SweepInboxService inboxService;

    @BeforeEach
    void setUp() {
        inboxRepository = mock(InvestChnInboxEventRepository.class);
        SweepRequestConsumerProperties properties =
                new SweepRequestConsumerProperties(true, 10, 5, 300L);
        inboxService = new SweepInboxService(inboxRepository, properties);
    }

    @Test
    @DisplayName("신규 이벤트면 inbox를 저장하고 PROCESSING으로 선점한다")
    void claimNewEvent() {
        SweepRequestedEvent event = validEvent();

        when(inboxRepository.findByIdempotencyKey(event.idempotencyKey()))
                .thenReturn(Optional.empty());
        when(inboxRepository.save(any())).thenAnswer(invocation -> {
            InvestChnInboxEvent inbox = invocation.getArgument(0);
            ReflectionTestUtils.setField(inbox, "inboxEventId", 1L);
            return inbox;
        });

        InboxClaimResult result = inboxService.claim(event, "{}");

        assertThat(result.claimed()).isTrue();
        assertThat(result.alreadyProcessed()).isFalse();
        assertThat(result.inboxEventId()).isEqualTo(1L);

        verify(inboxRepository).save(any(InvestChnInboxEvent.class));
    }

    @Test
    @DisplayName("이미 PROCESSED면 alreadyProcessed를 반환한다")
    void claimAlreadyProcessed() {
        InvestChnInboxEvent inbox = inboxWithStatus(SweepInboxProcessStatus.PROCESSED);
        when(inboxRepository.findByIdempotencyKey("SWEEP:POINT_LEDGER:1"))
                .thenReturn(Optional.of(inbox));

        InboxClaimResult result = inboxService.claim(validEvent(), "{}");

        assertThat(result.alreadyProcessed()).isTrue();
        assertThat(result.claimed()).isFalse();
        assertThat(result.inboxEventId()).isEqualTo(1L);
        verify(inboxRepository, never()).save(any());
    }

    @Test
    @DisplayName("PROCESSING이 timeout 전이면 inProgress를 반환한다")
    void claimProcessingNotTimedOut() {
        InvestChnInboxEvent inbox = inboxWithStatus(SweepInboxProcessStatus.PROCESSING);
        ReflectionTestUtils.setField(inbox, "updatedAt", LocalDateTime.now());

        when(inboxRepository.findByIdempotencyKey("SWEEP:POINT_LEDGER:1"))
                .thenReturn(Optional.of(inbox));

        InboxClaimResult result = inboxService.claim(validEvent(), "{}");

        assertThat(result.claimed()).isFalse();
        assertThat(result.alreadyProcessed()).isFalse();
        assertThat(result.inboxEventId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("PROCESSING이 timeout 되었으면 다시 PROCESSING으로 선점한다")
    void claimProcessingTimedOut() {
        InvestChnInboxEvent inbox = inboxWithStatus(SweepInboxProcessStatus.PROCESSING);
        ReflectionTestUtils.setField(inbox, "updatedAt", LocalDateTime.now().minusSeconds(301));

        when(inboxRepository.findByIdempotencyKey("SWEEP:POINT_LEDGER:1"))
                .thenReturn(Optional.of(inbox));

        InboxClaimResult result = inboxService.claim(validEvent(), "{}");

        assertThat(result.claimed()).isTrue();
        assertThat(inbox.getProcessStatus()).isEqualTo(SweepInboxProcessStatus.PROCESSING);
    }

    @Test
    @DisplayName("FAILED 상태면 재처리를 위해 PROCESSING으로 선점한다")
    void claimFailed() {
        InvestChnInboxEvent inbox = inboxWithStatus(SweepInboxProcessStatus.FAILED);

        when(inboxRepository.findByIdempotencyKey("SWEEP:POINT_LEDGER:1"))
                .thenReturn(Optional.of(inbox));

        InboxClaimResult result = inboxService.claim(validEvent(), "{}");

        assertThat(result.claimed()).isTrue();
        assertThat(inbox.getProcessStatus()).isEqualTo(SweepInboxProcessStatus.PROCESSING);
    }

    private InvestChnInboxEvent inboxWithStatus(SweepInboxProcessStatus status) {
        InvestChnInboxEvent inbox = InvestChnInboxEvent.received(
                "CARD_CHANNEL",
                "CARD-SWEEP-1",
                SweepEventType.SWEEP_REQUESTED,
                1L,
                "{}",
                "corr-1",
                "SWEEP:POINT_LEDGER:1"
        );
        ReflectionTestUtils.setField(inbox, "inboxEventId", 1L);
        ReflectionTestUtils.setField(inbox, "processStatus", status);
        ReflectionTestUtils.setField(inbox, "updatedAt", LocalDateTime.now());
        return inbox;
    }

    private SweepRequestedEvent validEvent() {
        return new SweepRequestedEvent(
                "CARD-SWEEP-1",
                "SWEEP_REQUESTED",
                "corr-1",
                "SWEEP:POINT_LEDGER:1",
                1L,
                "user-uuid",
                "card-user-uuid",
                1L,
                1L,
                "2026-05",
                1000L,
                1000L,
                100L,
                LocalDateTime.now()
        );
    }
}
