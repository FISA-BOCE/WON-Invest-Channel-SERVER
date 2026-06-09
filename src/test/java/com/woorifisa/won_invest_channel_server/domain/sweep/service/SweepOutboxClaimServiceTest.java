package com.woorifisa.won_invest_channel_server.domain.sweep.service;

import com.woorifisa.won_invest_channel_server.domain.sweep.model.InvestChnOutboxEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepEventType;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepOutboxPublishStatus;
import com.woorifisa.won_invest_channel_server.domain.sweep.repository.InvestChnOutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SweepOutboxClaimServiceTest {

    private InvestChnOutboxEventRepository outboxRepository;
    private SweepOutboxClaimService claimService;

    @BeforeEach
    void setUp() {
        outboxRepository = mock(InvestChnOutboxEventRepository.class);
        claimService = new SweepOutboxClaimService(outboxRepository);
    }

    @Test
    @DisplayName("발행 대상 outbox를 PROCESSING 상태로 선점하고 id 목록을 반환한다")
    void claimPublishTargets() {
        InvestChnOutboxEvent outbox = createOutbox();

        when(outboxRepository.findPublishTargets(
                anyCollection(),
                any(LocalDateTime.class),
                any(Pageable.class)
        )).thenReturn(List.of(outbox));

        List<Long> result = claimService.claimPublishTargets(20);

        assertThat(result).containsExactly(1L);
        assertThat(outbox.getPublishStatus()).isEqualTo(SweepOutboxPublishStatus.PROCESSING);
    }

    @Test
    @DisplayName("발행 대상이 없으면 빈 목록을 반환한다")
    void claimNoTargets() {
        when(outboxRepository.findPublishTargets(anyCollection(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of());

        List<Long> result = claimService.claimPublishTargets(20);

        assertThat(result).isEmpty();
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
