package com.woorifisa.won_invest_channel_server.domain.sweep.service;

import com.woorifisa.won_invest_channel_server.domain.sweep.dto.event.SweepRequestedEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.dto.response.InvestCoreSweepExecutionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SweepRequestSuccessService {

    private final SweepResultOutboxService resultOutboxService;
    private final SweepInboxService inboxService;

    @Transactional
    public void saveResultAndMarkProcessed(
            Long inboxEventId,
            SweepRequestedEvent event,
            InvestCoreSweepExecutionResponse response
    ) {
        if (response.completed()) {
            resultOutboxService.saveCompleted(event, response);
        } else {
            resultOutboxService.saveFailed(
                    event,
                    response.sweepExecutionId(),
                    response.failureCode(),
                    response.failureMessage()
            );
        }

        inboxService.markProcessed(inboxEventId);
    }
}
