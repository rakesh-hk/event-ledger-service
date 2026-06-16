package com.example.eventgateway.service;

import com.example.eventgateway.dto.EventRequest;
import com.example.eventgateway.dto.EventResponse;
import com.example.eventgateway.dto.TransactionRequest;
import com.example.eventgateway.entity.EventEntity;
import com.example.eventgateway.exception.ServiceUnavailableException;
import com.example.eventgateway.repository.EventRepository;
import com.example.eventgateway.metrics.EventMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final AccountServiceClient accountServiceClient;
    private final EventMetrics eventMetrics;

    public EventResponse submitEvent(EventRequest request, String traceId) {
        eventMetrics.incrementRequestCount();
        Optional<EventEntity> existing = eventRepository.findById(request.getEventId());

        if (existing.isPresent()) {
            return mapToResponse(existing.get());
        }

        EventEntity entity = mapToEntity(request);
        TransactionRequest transactionRequest = mapToTransactionRequest(request);

        try {
            accountServiceClient.postTransaction(request.getAccountId(), transactionRequest, traceId).join();
            EventEntity saved = eventRepository.save(entity);
            eventMetrics.incrementSuccessfulEvents();
            log.info("Event persisted and forwarded successfully: {}", request.getEventId());
            return mapToResponse(saved);
        } catch (CompletionException ex) {
            eventMetrics.incrementFailedEvents();
            log.warn("Account service call failed for eventId {}", request.getEventId(), ex.getCause());
            throw new ServiceUnavailableException("Account service unavailable", ex.getCause());
        } catch (DataIntegrityViolationException ex) {
            eventMetrics.incrementFailedEvents();
            log.warn("Event persistence failure for eventId {}", request.getEventId(), ex);
            throw new ServiceUnavailableException("Duplicate event or persistence error", ex);
        }
    }

    public EventResponse findEvent(String eventId) {
        return eventRepository.findById(eventId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
    }

    public List<EventResponse> findEventsByAccount(String accountId) {
        return eventRepository.findByAccountIdOrderByEventTimestampAsc(accountId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private EventEntity mapToEntity(EventRequest request) {
        EventEntity entity = new EventEntity();
        entity.setEventId(request.getEventId());
        entity.setAccountId(request.getAccountId());
        entity.setType(request.getType().name());
        entity.setAmount(request.getAmount());
        entity.setCurrency(request.getCurrency());
        entity.setEventTimestamp(request.getEventTimestamp());
        entity.setMetadata(request.getMetadata() == null ? null : request.getMetadata().toString());
        return entity;
    }

    private TransactionRequest mapToTransactionRequest(EventRequest request) {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setEventId(request.getEventId());
        transactionRequest.setType(request.getType());
        transactionRequest.setAmount(request.getAmount());
        transactionRequest.setCurrency(request.getCurrency());
        transactionRequest.setEventTimestamp(request.getEventTimestamp());
        return transactionRequest;
    }

    private EventResponse mapToResponse(EventEntity entity) {
        return EventResponse.builder()
                .eventId(entity.getEventId())
                .accountId(entity.getAccountId())
                .type(Enum.valueOf(com.example.eventgateway.dto.EventType.class, entity.getType()))
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .eventTimestamp(entity.getEventTimestamp())
                .metadata(entity.getMetadata() == null ? null : Map.of("raw", entity.getMetadata()))
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
