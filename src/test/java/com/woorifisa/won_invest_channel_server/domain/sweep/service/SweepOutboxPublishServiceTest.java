package com.woorifisa.won_invest_channel_server.domain.sweep.service;

import com.woorifisa.won_invest_channel_server.domain.sweep.dto.command.SweepOutboxPublishMessage;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepEventType;
import com.woorifisa.won_invest_channel_server.global.config.SqsProperties;
import com.woorifisa.won_invest_channel_server.global.config.SweepOutboxPublisherProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SweepOutboxPublishServiceTest {

    private SweepOutboxStatusService statusService;
    private SqsClient sqsClient;
    private SweepOutboxPublishService publishService;

    @BeforeEach
    void setUp() {
        statusService = mock(SweepOutboxStatusService.class);
        sqsClient = mock(SqsClient.class);

        SqsProperties sqsProperties = new SqsProperties(
                "ap-northeast-2",
                "http://localhost:4566",
                "request-queue-url",
                "result-queue-url"
        );

        SweepOutboxPublisherProperties publisherProperties =
                new SweepOutboxPublisherProperties(true, 20, 3, 10000L);

        publishService = new SweepOutboxPublishService(
                statusService,
                sqsClient,
                sqsProperties,
                publisherProperties
        );
    }

    @Test
    @DisplayName("SQS 발행 성공 시 결과 outbox를 PUBLISHED 처리한다")
    void publishSuccess() {
        SweepOutboxPublishMessage message = message();

        when(statusService.getPublishMessage(1L)).thenReturn(message);
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().messageId("message-1").build());

        publishService.publish(1L);

        ArgumentCaptor<SendMessageRequest> requestCaptor =
                ArgumentCaptor.forClass(SendMessageRequest.class);

        verify(sqsClient).sendMessage(requestCaptor.capture());

        SendMessageRequest request = requestCaptor.getValue();

        assertThat(request.queueUrl()).isEqualTo("result-queue-url");
        assertThat(request.messageBody()).isEqualTo("{}");
        assertThat(request.messageGroupId()).isEqualTo("CORR-1");
        assertThat(request.messageDeduplicationId()).isEqualTo("SWEEP:POINT_LEDGER:1");

        verify(statusService).markPublished(1L);
        verify(statusService, never()).markPublishFailed(anyLong(), anyString(), anyInt());
    }

    @Test
    @DisplayName("SQS 발행 실패 시 결과 outbox를 RETRY 또는 FAILED 처리한다")
    void publishFailed() {
        SweepOutboxPublishMessage message = message();

        when(statusService.getPublishMessage(1L)).thenReturn(message);
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(new RuntimeException("SQS timeout"));

        publishService.publish(1L);

        verify(statusService).markPublishFailed(1L, "SQS timeout", 3);
        verify(statusService, never()).markPublished(anyLong());
    }

    @Test
    @DisplayName("발행 메시지 조회에 실패하면 SQS 발행과 상태 변경을 하지 않는다")
    void getPublishMessageFailed() {
        when(statusService.getPublishMessage(1L))
                .thenThrow(new IllegalStateException("invalid state"));

        publishService.publish(1L);

        verify(sqsClient, never()).sendMessage(any(SendMessageRequest.class));
        verify(statusService, never()).markPublished(anyLong());
        verify(statusService, never()).markPublishFailed(anyLong(), anyString(), anyInt());
    }

    private SweepOutboxPublishMessage message() {
        return new SweepOutboxPublishMessage(
                1L,
                "INVEST-SWEEP-1",
                SweepEventType.SWEEP_INVESTMENT_COMPLETED,
                1L,
                "CORR-1",
                "SWEEP:POINT_LEDGER:1",
                "{}"
        );
    }
}
