package com.woorifisa.won_invest_channel_server.domain.admin.service;

import com.woorifisa.won_invest_channel_server.domain.sweep.exception.code.SweepErrorCode;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.InvestChnOutboxEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepEventType;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepOutboxPublishStatus;
import com.woorifisa.won_invest_channel_server.domain.sweep.repository.InvestChnOutboxEventRepository;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminInvestOutboxEventServiceTest {

    private InvestChnOutboxEventRepository investChnOutboxEventRepository;
    private AdminInvestOutboxEventService service;

    @BeforeEach
    void setUp() {
        investChnOutboxEventRepository = mock(InvestChnOutboxEventRepository.class);
        service = new AdminInvestOutboxEventService(investChnOutboxEventRepository);
    }

    @Test
    @DisplayName("SQS 발행 실패로 FAILED 된 증권 Outbox 이벤트를 RETRY 상태로 되돌린다")
    void retryFailedOutboxEvent() {
        InvestChnOutboxEvent outbox = createOutbox();
        outbox.markProcessing();
        outbox.markPublishFailed("QueueDoesNotExistException: The specified queue does not exist.", 1);

        when(investChnOutboxEventRepository.findById(1L)).thenReturn(Optional.of(outbox));

        var response = service.retryOutboxEvent(1L);

        assertThat(outbox.getPublishStatus()).isEqualTo(SweepOutboxPublishStatus.RETRY);
        assertThat(outbox.getNextRetryAt()).isNotNull();
        assertThat(response.retryable()).isTrue();
        assertThat(response.retryDisabledReason()).isNull();
    }

    @Test
    @DisplayName("이미 발행 완료된 증권 Outbox 이벤트는 수동 재처리할 수 없다")
    void retryPublishedOutboxEventFails() {
        InvestChnOutboxEvent outbox = createOutbox();
        outbox.markProcessing();
        outbox.markPublished();

        when(investChnOutboxEventRepository.findById(1L)).thenReturn(Optional.of(outbox));

        assertThatThrownBy(() -> service.retryOutboxEvent(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SweepErrorCode.SWEEP_OUTBOX_RETRY_NOT_ALLOWED);
    }

    private InvestChnOutboxEvent createOutbox() {
        InvestChnOutboxEvent outbox = InvestChnOutboxEvent.pending(
                "INVEST-SWEEP-RESULT-1",
                SweepEventType.SWEEP_INVESTMENT_COMPLETED,
                1L,
                10L,
                "{\"eventType\":\"SWEEP_INVESTMENT_COMPLETED\",\"sweepRequestId\":1}",
                "correlation-id",
                "SWEEP:RESULT:1"
        );
        ReflectionTestUtils.setField(outbox, "outboxEventId", 1L);
        return outbox;
    }
}
