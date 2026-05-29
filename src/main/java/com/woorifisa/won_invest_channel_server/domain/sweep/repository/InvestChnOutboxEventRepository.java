package com.woorifisa.won_invest_channel_server.domain.sweep.repository;

import com.woorifisa.won_invest_channel_server.domain.sweep.model.InvestChnOutboxEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepEventType;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepOutboxPublishStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InvestChnOutboxEventRepository extends JpaRepository<InvestChnOutboxEvent, Long> {
    Optional<InvestChnOutboxEvent> findByIdempotencyKeyAndEventType(String idempotencyKey, SweepEventType eventType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select o
            from InvestChnOutboxEvent o
            where o.publishStatus in :statuses
              and (o.nextRetryAt is null or o.nextRetryAt <= :now)
            order by o.outboxEventId asc
            """)
    List<InvestChnOutboxEvent> findPublishTargets(
            @Param("statuses") Collection<SweepOutboxPublishStatus> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}
