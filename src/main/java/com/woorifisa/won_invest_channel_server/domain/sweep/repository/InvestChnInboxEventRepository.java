package com.woorifisa.won_invest_channel_server.domain.sweep.repository;

import com.woorifisa.won_invest_channel_server.domain.sweep.model.InvestChnInboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvestChnInboxEventRepository extends JpaRepository<InvestChnInboxEvent, Long> {
    Optional<InvestChnInboxEvent> findByIdempotencyKey(String idempotencyKey);
}
