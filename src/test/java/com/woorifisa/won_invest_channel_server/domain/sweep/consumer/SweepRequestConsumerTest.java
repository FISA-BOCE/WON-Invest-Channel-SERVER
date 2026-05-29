package com.woorifisa.won_invest_channel_server.domain.sweep.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.woorifisa.won_invest_channel_server.domain.sweep.dto.command.InboxClaimResult;
import com.woorifisa.won_invest_channel_server.domain.sweep.dto.event.SweepRequestedEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.service.SweepInboxService;
import com.woorifisa.won_invest_channel_server.domain.sweep.service.SweepRequestProcessService;
import com.woorifisa.won_invest_channel_server.global.config.SqsProperties;
import com.woorifisa.won_invest_channel_server.global.config.SweepRequestConsumerProperties;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_channel_server.domain.sweep.exception.code.SweepErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SweepRequestConsumerTest {

    private SqsClient sqsClient;
    private SweepInboxService inboxService;
    private SweepRequestProcessService processService;
    private SweepRequestConsumer consumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        sqsClient = mock(SqsClient.class);
        inboxService = mock(SweepInboxService.class);
        processService = mock(SweepRequestProcessService.class);

        SqsProperties sqsProperties = new SqsProperties(
                "ap-northeast-2",
                "http://localhost:4566",
                "http://localhost:4566/000000000000/won-card-sweep-request-queue.fifo"
        );
        SweepRequestConsumerProperties properties =
                new SweepRequestConsumerProperties(true, 10, 5, 300L);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        consumer = new SweepRequestConsumer(
                sqsClient,
                sqsProperties,
                properties,
                objectMapper,
                inboxService,
                processService
        );
    }

    @Test
    @DisplayName("정상 메시지는 validate, claim, process 후 SQS 메시지를 삭제한다")
    void pollSuccess() throws Exception {
        SweepRequestedEvent event = validEvent();
        String body = objectMapper.writeValueAsString(event);
        Message message = message(body);

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message).build());
        when(inboxService.claim(event, body))
                .thenReturn(InboxClaimResult.claimed(1L));

        consumer.poll();

        verify(processService).validate(event);
        verify(inboxService).claim(event, body);
        verify(processService).process(1L, event);
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("이미 처리된 메시지는 Core 처리 없이 SQS 메시지를 삭제한다")
    void pollAlreadyProcessed() throws Exception {
        SweepRequestedEvent event = validEvent();
        String body = objectMapper.writeValueAsString(event);
        Message message = message(body);

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message).build());
        when(inboxService.claim(event, body))
                .thenReturn(InboxClaimResult.alreadyProcessed(1L));

        consumer.poll();

        verify(processService).validate(event);
        verify(processService, never()).process(any(), any());
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("claim 결과가 inProgress이면 SQS 메시지를 삭제하지 않는다")
    void pollInProgress() throws Exception {
        SweepRequestedEvent event = validEvent();
        String body = objectMapper.writeValueAsString(event);
        Message message = message(body);

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message).build());
        when(inboxService.claim(event, body))
                .thenReturn(InboxClaimResult.inProgress(1L));

        consumer.poll();

        verify(processService, never()).process(any(), any());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("validate 실패 시 inbox claim과 SQS 삭제를 하지 않는다")
    void pollValidateFailed() throws Exception {
        SweepRequestedEvent event = validEventWithoutIdempotencyKey();
        String body = objectMapper.writeValueAsString(event);
        Message message = message(body);

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message).build());
        doThrow(new BusinessException(SweepErrorCode.INVALID_SWEEP_EVENT_PAYLOAD))
                .when(processService).validate(event);

        consumer.poll();

        verify(inboxService, never()).claim(any(), any());
        verify(processService, never()).process(any(), any());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("process 실패 시 SQS 메시지를 삭제하지 않는다")
    void pollProcessFailed() throws Exception {
        SweepRequestedEvent event = validEvent();
        String body = objectMapper.writeValueAsString(event);
        Message message = message(body);

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message).build());
        when(inboxService.claim(event, body))
                .thenReturn(InboxClaimResult.claimed(1L));
        doThrow(new RuntimeException("core timeout"))
                .when(processService).process(1L, event);

        consumer.poll();

        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    private Message message(String body) {
        return Message.builder()
                .messageId("message-1")
                .receiptHandle("receipt-1")
                .body(body)
                .build();
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

    private SweepRequestedEvent validEventWithoutIdempotencyKey() {
        return new SweepRequestedEvent(
                "CARD-SWEEP-1",
                "SWEEP_REQUESTED",
                "corr-1",
                null,
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
