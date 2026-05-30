package com.woorifisa.won_invest_channel_server.domain.sweep.service;

import com.woorifisa.won_invest_channel_server.domain.sweep.dto.command.SweepOutboxPublishMessage;
import com.woorifisa.won_invest_channel_server.global.config.SqsProperties;
import com.woorifisa.won_invest_channel_server.global.config.SweepOutboxPublisherProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class SweepOutboxPublishService {

    private final SweepOutboxStatusService statusService;
    private final SqsClient sqsClient;
    private final SqsProperties sqsProperties;
    private final SweepOutboxPublisherProperties publisherProperties;

    public void publish(Long outboxEventId) {
        SweepOutboxPublishMessage message;

        try {
            message = statusService.getPublishMessage(outboxEventId);
        } catch (Exception e) {
            statusService.markPublishFailed(
                    outboxEventId,
                    e.getMessage(),
                    publisherProperties.maxRetryCount()
            );
            log.warn("스윕 결과 outbox 발행 메시지 조회 실패. outboxEventId={}", outboxEventId, e);
            return;
        }

        try {
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(sqsProperties.sweepResultQueueUrl())
                    .messageBody(message.payload())
                    .messageGroupId(message.correlationId())
                    .messageDeduplicationId(message.idempotencyKey())
                    .build();

            sqsClient.sendMessage(request);

            statusService.markPublished(outboxEventId);

            log.info("스윕 결과 outbox 발행 성공. outboxEventId={}, eventId={}, sweepRequestId={}",
                    message.outboxEventId(), message.eventId(), message.sweepRequestId());
        } catch (Exception e) {
            statusService.markPublishFailed(
                    outboxEventId,
                    e.getMessage(),
                    publisherProperties.maxRetryCount()
            );

            log.warn("스윕 결과 outbox 발행 실패. outboxEventId={}, eventId={}, sweepRequestId={}",
                    message.outboxEventId(), message.eventId(), message.sweepRequestId(), e);
        }
    }
}
