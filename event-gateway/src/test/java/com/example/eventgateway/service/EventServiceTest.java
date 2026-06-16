package com.example.eventgateway.service;

import com.example.eventgateway.dto.EventRequest;
import com.example.eventgateway.dto.EventType;
import com.example.eventgateway.entity.EventEntity;
import com.example.eventgateway.metrics.EventMetrics;
import com.example.eventgateway.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private EventMetrics eventMetrics;

    @InjectMocks
    private EventService eventService;

    private EventRequest request;

    @BeforeEach
    void setUp() {
        request = new EventRequest();
        request.setEventId("evt-123");
        request.setAccountId("acct-01");
        request.setType(EventType.DEPOSIT);
        request.setAmount(BigDecimal.valueOf(100));
        request.setCurrency("USD");
        request.setEventTimestamp(Instant.parse("2026-01-01T12:00:00Z"));
    }

    @Test
    void shouldPersistEventWhenAccountServiceSucceeds() {
        given(eventRepository.findById(request.getEventId())).willReturn(Optional.empty());
        given(accountServiceClient.postTransaction(eq(request.getAccountId()), any(), eq(null)))
                .willReturn(CompletableFuture.completedFuture(null));
        given(eventRepository.save(any(EventEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

        var response = eventService.submitEvent(request, null);

        assertThat(response.getEventId()).isEqualTo(request.getEventId());
        verify(eventMetrics).incrementRequestCount();
        verify(eventMetrics).incrementSuccessfulEvents();
    }

    @Test
    void shouldReturnExistingEventForDuplicateEventId() {
        EventEntity existing = new EventEntity();
        existing.setEventId(request.getEventId());
        existing.setAccountId(request.getAccountId());
        existing.setType(request.getType().name());
        existing.setAmount(request.getAmount());
        existing.setCurrency(request.getCurrency());
        existing.setEventTimestamp(request.getEventTimestamp());
        existing.setCreatedAt(Instant.now());

        given(eventRepository.findById(request.getEventId())).willReturn(Optional.of(existing));

        var response = eventService.submitEvent(request, null);

        assertThat(response.getEventId()).isEqualTo(request.getEventId());
        verify(eventMetrics).incrementRequestCount();
        verify(eventMetrics).incrementSuccessfulEvents();
    }

    @Test
    void shouldThrowWhenAccountServiceUnavailable() {
        given(eventRepository.findById(request.getEventId())).willReturn(Optional.empty());
        given(accountServiceClient.postTransaction(eq(request.getAccountId()), any(), eq(null)))
                .willReturn(CompletableFuture.failedFuture(new RuntimeException("unreachable")));

        assertThatThrownBy(() -> eventService.submitEvent(request, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Account service unavailable");
        verify(eventMetrics).incrementRequestCount();
        verify(eventMetrics).incrementFailedEvents();
    }
}
