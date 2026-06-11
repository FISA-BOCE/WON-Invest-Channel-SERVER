package com.woorifisa.won_invest_channel_server.global.scheduler;

import com.woorifisa.won_invest_channel_server.global.logging.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorLogCleanupScheduler {

    private final ErrorLogRepository errorLogRepository;

    @Value("${logging.error-log.retention-days:7}")
    private int retentionDays;

    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
    @Transactional
    public void deleteExpiredLogs() {
        LocalDateTime threshold = LocalDateTime.now(ZoneOffset.UTC).minusDays(retentionDays);
        int deleted = errorLogRepository.deleteByCreatedAtBefore(threshold);
        log.info("error log cleanup: deleted={}", deleted);
    }
}