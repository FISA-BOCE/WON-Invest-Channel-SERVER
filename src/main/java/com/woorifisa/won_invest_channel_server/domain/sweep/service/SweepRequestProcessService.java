package com.woorifisa.won_invest_channel_server.domain.sweep.service;

import com.woorifisa.won_invest_channel_server.domain.sweep.dto.event.SweepRequestedEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.dto.request.InvestCoreSweepExecutionRequest;
import com.woorifisa.won_invest_channel_server.domain.sweep.dto.response.InvestCoreSweepExecutionResponse;
import com.woorifisa.won_invest_channel_server.domain.sweep.exception.code.SweepErrorCode;
import com.woorifisa.won_invest_channel_server.domain.sweep.external.InvestCoreSweepExecutionApi;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SweepRequestProcessService {

    private final SweepInboxService inboxService;
    private final SweepResultOutboxService resultOutboxService;
    private final InvestCoreSweepExecutionApi investCoreApi;

    public void process(Long inboxEventId, SweepRequestedEvent event) {
        try {
            validate(event);

            InvestCoreSweepExecutionResponse response =
                    investCoreApi.executeSweep(InvestCoreSweepExecutionRequest.from(event));

            if (response == null || response.status() == null) {
                throw new BusinessException(SweepErrorCode.SWEEP_CORE_UNAVAILABLE);
            }

            if (response.completed()) {
                resultOutboxService.saveCompleted(event, response);
            } else if (response.failed()) {
                resultOutboxService.saveFailed(
                        event,
                        response.sweepExecutionId(),
                        response.failureCode(),
                        response.failureMessage()
                );
            } else {
                throw new BusinessException(SweepErrorCode.SWEEP_CORE_UNAVAILABLE);
            }

            inboxService.markProcessed(inboxEventId);
        } catch (RuntimeException e) {
            inboxService.markFailed(inboxEventId, e.getMessage());
            throw e;
        } catch (Exception e) {
            inboxService.markFailed(inboxEventId, e.getMessage());
            throw new BusinessException(SweepErrorCode.SWEEP_CORE_UNAVAILABLE);
        }
    }

    public void validate(SweepRequestedEvent event) {
        if (!"SWEEP_REQUESTED".equals(event.eventType())) {
            throw new BusinessException(SweepErrorCode.INVALID_SWEEP_EVENT_TYPE);
        }

        if (isBlank(event.eventId()) || isBlank(event.correlationId()) || isBlank(event.idempotencyKey())
                || event.sweepRequestId() == null || isBlank(event.userUuid()) || isBlank(event.cardUserUuid())
                || event.pointLedgerId() == null || event.pointAmount() == null || event.krwAmount() == null
                || event.etfId() == null || isBlank(event.baseMonth())) {
            throw new BusinessException(SweepErrorCode.INVALID_SWEEP_EVENT_PAYLOAD);
        }

        if (event.krwAmount() < 1) {
            throw new BusinessException(SweepErrorCode.INVALID_SWEEP_AMOUNT);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
