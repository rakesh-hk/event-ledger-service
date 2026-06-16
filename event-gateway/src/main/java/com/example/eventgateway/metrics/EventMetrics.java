package com.example.eventgateway.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class EventMetrics {

    private final MeterRegistry meterRegistry;
    private Counter requestCounter;
    private Counter successfulEventCounter;
    private Counter failedEventCounter;

    public EventMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        requestCounter = Counter.builder("event.gateway.request.count")
                .description("Total number of incoming event requests")
                .register(meterRegistry);
        successfulEventCounter = Counter.builder("event.gateway.successful.events")
                .description("Total number of successfully processed events")
                .register(meterRegistry);
        failedEventCounter = Counter.builder("event.gateway.failed.events")
                .description("Total number of failed event submissions")
                .register(meterRegistry);
    }

    public void incrementRequestCount() {
        requestCounter.increment();
    }

    public void incrementSuccessfulEvents() {
        successfulEventCounter.increment();
    }

    public void incrementFailedEvents() {
        failedEventCounter.increment();
    }
}
