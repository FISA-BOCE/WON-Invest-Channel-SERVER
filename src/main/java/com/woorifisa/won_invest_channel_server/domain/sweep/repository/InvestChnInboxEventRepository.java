package com.woorifisa.won_invest_channel_server.domain.sweep.repository;

import com.woorifisa.won_invest_channel_server.domain.sweep.model.InvestChnInboxEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepEventType;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepInboxProcessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface InvestChnInboxEventRepository extends JpaRepository<InvestChnInboxEvent, Long> {
    Optional<InvestChnInboxEvent> findByIdempotencyKey(String idempotencyKey);

    @Query("""
            select i
            from InvestChnInboxEvent i
            where (:processStatus is null or i.processStatus = :processStatus)
              and (:eventType is null or i.eventType = :eventType)
              and (:sweepRequestId is null or i.sweepRequestId = :sweepRequestId)
              and (:createdFrom is null or i.createdAt >= :createdFrom)
              and (:createdTo is null or i.createdAt < :createdTo)
            order by i.inboxEventId desc
            """)
    Page<InvestChnInboxEvent> findAdminInboxEvents(
            @Param("processStatus") SweepInboxProcessStatus processStatus,
            @Param("eventType") SweepEventType eventType,
            @Param("sweepRequestId") Long sweepRequestId,
            @Param("createdFrom") LocalDateTime createdFrom,
            @Param("createdTo") LocalDateTime createdTo,
            Pageable pageable
    );

    @Query("""
            select count(i)
            from InvestChnInboxEvent i
            where (:processStatus is null or i.processStatus = :processStatus)
              and (:eventType is null or i.eventType = :eventType)
              and (:sweepRequestId is null or i.sweepRequestId = :sweepRequestId)
              and (:createdFrom is null or i.createdAt >= :createdFrom)
              and (:createdTo is null or i.createdAt < :createdTo)
            """)
    long countAdminInboxEvents(
            @Param("processStatus") SweepInboxProcessStatus processStatus,
            @Param("eventType") SweepEventType eventType,
            @Param("sweepRequestId") Long sweepRequestId,
            @Param("createdFrom") LocalDateTime createdFrom,
            @Param("createdTo") LocalDateTime createdTo
    );
}
