package com.woorifisa.won_invest_channel_server.domain.admin.service;

import com.woorifisa.won_invest_channel_server.domain.admin.dto.response.AdminInvestInboxEventItemResponse;
import com.woorifisa.won_invest_channel_server.domain.admin.dto.response.AdminInvestInboxEventListResponse;
import com.woorifisa.won_invest_channel_server.domain.admin.dto.response.AdminInvestInboxEventSummaryResponse;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.InvestChnInboxEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepEventType;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepInboxProcessStatus;
import com.woorifisa.won_invest_channel_server.domain.sweep.repository.InvestChnInboxEventRepository;
import com.woorifisa.won_invest_channel_server.global.exception.code.CommonErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInvestInboxEventService {

    private static final int MAX_PAGE_SIZE = 100;

    private final InvestChnInboxEventRepository investChnInboxEventRepository;

    public AdminInvestInboxEventListResponse getInboxEvents(
            String systemType,
            String status,
            SweepEventType eventType,
            Long sweepRequestId,
            int page,
            int size
    ) {
        validateSystemType(systemType);

        SweepInboxProcessStatus processStatus = mapStatus(status);
        Pageable pageable = PageRequest.of(normalizePage(page), normalizeSize(size));
        Page<InvestChnInboxEvent> inboxEvents = investChnInboxEventRepository.findAdminInboxEvents(
                processStatus,
                eventType,
                sweepRequestId,
                null,
                null,
                pageable
        );

        return new AdminInvestInboxEventListResponse(
                getSummary(eventType, sweepRequestId),
                inboxEvents.getContent()
                        .stream()
                        .map(AdminInvestInboxEventItemResponse::from)
                        .toList(),
                inboxEvents.getNumber(),
                inboxEvents.getSize(),
                inboxEvents.getTotalElements(),
                inboxEvents.getTotalPages()
        );
    }

    public AdminInvestInboxEventItemResponse getInboxEvent(Long inboxEventId) {
        InvestChnInboxEvent inbox = investChnInboxEventRepository.findById(inboxEventId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND));

        return AdminInvestInboxEventItemResponse.from(inbox);
    }

    public AdminInvestInboxEventSummaryResponse getSummary(
            SweepEventType eventType,
            Long sweepRequestId
    ) {
        long processedCount = investChnInboxEventRepository.countAdminInboxEvents(
                SweepInboxProcessStatus.PROCESSED,
                eventType,
                sweepRequestId,
                null,
                null
        );
        long failedCount = investChnInboxEventRepository.countAdminInboxEvents(
                SweepInboxProcessStatus.FAILED,
                eventType,
                sweepRequestId,
                null,
                null
        );
        long processingCount = investChnInboxEventRepository.countAdminInboxEvents(
                SweepInboxProcessStatus.PROCESSING,
                eventType,
                sweepRequestId,
                null,
                null
        );
        long receivedCount = investChnInboxEventRepository.countAdminInboxEvents(
                SweepInboxProcessStatus.RECEIVED,
                eventType,
                sweepRequestId,
                null,
                null
        );

        return new AdminInvestInboxEventSummaryResponse(
                processedCount + failedCount + processingCount + receivedCount,
                processedCount,
                failedCount,
                processingCount,
                receivedCount
        );
    }

    private void validateSystemType(String systemType) {
        if (systemType == null || systemType.isBlank() || "ALL".equalsIgnoreCase(systemType)) {
            return;
        }

        if (!"INVEST".equalsIgnoreCase(systemType)) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private SweepInboxProcessStatus mapStatus(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            return null;
        }

        String normalizedStatus = status.toUpperCase(Locale.ROOT);
        return switch (normalizedStatus) {
            case "RECEIVED" -> SweepInboxProcessStatus.RECEIVED;
            case "PROCESSING" -> SweepInboxProcessStatus.PROCESSING;
            case "PROCESSED", "COMPLETED" -> SweepInboxProcessStatus.PROCESSED;
            case "FAILED" -> SweepInboxProcessStatus.FAILED;
            default -> throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        };
    }

    private int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return 20;
        }

        return Math.min(size, MAX_PAGE_SIZE);
    }
}
