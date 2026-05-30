package com.woorifisa.won_invest_channel_server.domain.sweep.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorifisa.won_invest_channel_server.domain.sweep.dto.command.InboxClaimResult;
import com.woorifisa.won_invest_channel_server.domain.sweep.dto.event.SweepRequestedEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.service.SweepInboxService;
import com.woorifisa.won_invest_channel_server.domain.sweep.service.SweepRequestProcessService;
import com.woorifisa.won_invest_channel_server.global.config.SqsProperties;
import com.woorifisa.won_invest_channel_server.global.config.SweepRequestConsumerProperties;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class SweepRequestConsumer {

    private final SqsClient sqsClient;
    private final SqsProperties sqsProperties;
    private final SweepRequestConsumerProperties properties;
    private final ObjectMapper objectMapper;
    private final SweepInboxService inboxService;
    private final SweepRequestProcessService processService;

    @Scheduled(fixedDelayString = "${sweep.consumer.fixed-delay-ms:10000}")
    public void poll() {
        if (!properties.enabled()) {
            return;
        }

        ReceiveMessageResponse response = sqsClient.receiveMessage(
                ReceiveMessageRequest.builder()
                        .queueUrl(sqsProperties.sweepRequestQueueUrl())
                        .maxNumberOfMessages(properties.maxMessages())
                        .waitTimeSeconds(properties.waitTimeSeconds())
                        .build()
        );

        for (Message message : response.messages()) {
            handle(message);
        }
    }

    private void handle(Message message) {
        try {
            SweepRequestedEvent event = objectMapper.readValue(message.body(), SweepRequestedEvent.class);

            try {
                processService.validate(event);
            } catch (BusinessException e) {
                log.warn("유효하지 않은 스윕 요청 메시지 스킵. messageId={}", message.messageId(), e);
                deleteMessage(message);
                return;
            }

            InboxClaimResult claimResult = inboxService.claim(event, message.body());

            if (claimResult.alreadyProcessed()) {
                deleteMessage(message);
                return;
            }

            if (!claimResult.claimed()) {
                return;
            }

            processService.process(claimResult.inboxEventId(), event);
            deleteMessage(message);

            log.info("스윕 요청 메시지 처리 완료. messageId={}, idempotencyKey={}",
                    message.messageId(), event.idempotencyKey());
        } catch (Exception e) {
            log.warn("스윕 요청 메시지 처리 실패. messageId={}", message.messageId(), e);
        }
    }

    private void deleteMessage(Message message) {
        sqsClient.deleteMessage(
                DeleteMessageRequest.builder()
                        .queueUrl(sqsProperties.sweepRequestQueueUrl())
                        .receiptHandle(message.receiptHandle())
                        .build()
        );
    }
}
