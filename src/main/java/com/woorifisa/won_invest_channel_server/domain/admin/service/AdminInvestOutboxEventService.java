package com.woorifisa.won_invest_channel_server.domain.admin.service;

import com.woorifisa.won_invest_channel_server.domain.admin.dto.response.AdminInvestOutboxEventItemResponse;
import com.woorifisa.won_invest_channel_server.domain.admin.dto.response.AdminInvestOutboxEventListResponse;
import com.woorifisa.won_invest_channel_server.domain.admin.dto.response.AdminInvestOutboxEventSummaryResponse;
import com.woorifisa.won_invest_channel_server.domain.sweep.exception.code.SweepErrorCode;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.InvestChnOutboxEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepEventType;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepOutboxPublishStatus;
import com.woorifisa.won_invest_channel_server.domain.sweep.repository.InvestChnOutboxEventRepository;
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
public class AdminInvestOutboxEventService {

    private static final int MAX_PAGE_SIZE = 100;

    private final InvestChnOutboxEventRepository investChnOutboxEventRepository;

    public AdminInvestOutboxEventListResponse getOutboxEvents(
            String systemType,
            String status,
            SweepEventType eventType,
            Long sweepRequestId,
            int page,
            int size
    ) {
        validateSystemType(systemType);

        SweepOutboxPublishStatus publishStatus = mapStatus(status);
        Pageable pageable = PageRequest.of(normalizePage(page), normalizeSize(size));
        Page<InvestChnOutboxEvent> outboxEvents = investChnOutboxEventRepository.findAdminOutboxEvents(
                publishStatus,
                eventType,
                sweepRequestId,
                null,
                null,
                pageable
        );

        return new AdminInvestOutboxEventListResponse(
                getSummary(eventType, sweepRequestId),
                outboxEvents.getContent()
                        .stream()
                        .map(AdminInvestOutboxEventItemResponse::from)
                        .toList(),
                outboxEvents.getNumber(),
                outboxEvents.getSize(),
                outboxEvents.getTotalElements(),
                outboxEvents.getTotalPages()
        );
    }

    public AdminInvestOutboxEventItemResponse getOutboxEvent(Long outboxEventId) {
        InvestChnOutboxEvent outbox = investChnOutboxEventRepository.findById(outboxEventId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND));

        return AdminInvestOutboxEventItemResponse.from(outbox);
    }

    @Transactional
    public AdminInvestOutboxEventItemResponse retryOutboxEvent(Long outboxEventId) {
        InvestChnOutboxEvent outbox = investChnOutboxEventRepository.findById(outboxEventId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND));
        AdminInvestOutboxEventItemResponse response = AdminInvestOutboxEventItemResponse.from(outbox);

        if (!response.retryable()) {
            throw new BusinessException(
                    SweepErrorCode.SWEEP_OUTBOX_RETRY_NOT_ALLOWED,
                    response.retryDisabledReason()
            );
        }

        outbox.markRetryRequested();
        return AdminInvestOutboxEventItemResponse.from(outbox);
    }

    public AdminInvestOutboxEventSummaryResponse getSummary(
            SweepEventType eventType,
            Long sweepRequestId
    ) {
        long publishedCount = investChnOutboxEventRepository.countAdminOutboxEvents(
                SweepOutboxPublishStatus.PUBLISHED,
                eventType,
                sweepRequestId,
                null,
                null
        );
        long failedCount = investChnOutboxEventRepository.countAdminOutboxEvents(
                SweepOutboxPublishStatus.FAILED,
                eventType,
                sweepRequestId,
                null,
                null
        );
        long retryingCount = investChnOutboxEventRepository.countAdminOutboxEvents(
                SweepOutboxPublishStatus.RETRY,
                eventType,
                sweepRequestId,
                null,
                null
        );
        long pendingCount = investChnOutboxEventRepository.countAdminOutboxEvents(
                SweepOutboxPublishStatus.PENDING,
                eventType,
                sweepRequestId,
                null,
                null
        ) + investChnOutboxEventRepository.countAdminOutboxEvents(
                SweepOutboxPublishStatus.PROCESSING,
                eventType,
                sweepRequestId,
                null,
                null
        );

        return new AdminInvestOutboxEventSummaryResponse(
                publishedCount + failedCount + retryingCount + pendingCount,
                publishedCount,
                failedCount,
                retryingCount,
                pendingCount
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

    private SweepOutboxPublishStatus mapStatus(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            return null;
        }

        String normalizedStatus = status.toUpperCase(Locale.ROOT);
        return switch (normalizedStatus) {
            case "PENDING" -> SweepOutboxPublishStatus.PENDING;
            case "PROCESSING" -> SweepOutboxPublishStatus.PROCESSING;
            case "PUBLISHED" -> SweepOutboxPublishStatus.PUBLISHED;
            case "RETRY", "RETRYING" -> SweepOutboxPublishStatus.RETRY;
            case "FAILED" -> SweepOutboxPublishStatus.FAILED;
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
