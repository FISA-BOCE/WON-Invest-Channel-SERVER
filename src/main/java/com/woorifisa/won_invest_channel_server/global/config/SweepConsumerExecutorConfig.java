package com.woorifisa.won_invest_channel_server.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@RequiredArgsConstructor
public class SweepConsumerExecutorConfig {

    private final SweepRequestConsumerProperties properties;

    @Bean(name = "sweepRequestConsumerExecutor")
    public ThreadPoolExecutor sweepRequestConsumerExecutor() {
        int workerCount = properties.workerCount();
        AtomicInteger sequence = new AtomicInteger(1);

        return new ThreadPoolExecutor(
                workerCount,
                workerCount,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(workerCount * 4),
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("sweep-request-consumer-" + sequence.getAndIncrement());
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
