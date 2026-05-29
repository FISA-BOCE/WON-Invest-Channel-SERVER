package com.woorifisa.won_invest_channel_server.domain.sweep.repository;

import com.woorifisa.won_invest_channel_server.domain.sweep.model.InvestChnOutboxEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvestChnOutboxEventRepository extends JpaRepository<InvestChnOutboxEvent, Long> {
    Optional<InvestChnOutboxEvent> findByIdempotencyKeyAndEventType(String idempotencyKey, SweepEventType eventType);
}
