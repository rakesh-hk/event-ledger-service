package com.example.accountservice.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class AccountMetrics {

    private final MeterRegistry meterRegistry;
    private Counter transactionCounter;

    public AccountMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        transactionCounter = Counter.builder("account.service.transaction.count")
                .description("Total number of account transactions")
                .register(meterRegistry);
    }

    public void incrementTransactionCount() {
        transactionCounter.increment();
    }
}
