package com.example.eventgateway.controller;

import com.example.eventgateway.dto.EventRequest;
import com.example.eventgateway.dto.EventResponse;
import com.example.eventgateway.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> submitEvent(
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId,
            @Valid @RequestBody EventRequest request
    ) {
        EventResponse response = eventService.submitEvent(request, traceId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable("eventId") String eventId) {
        return ResponseEntity.ok(eventService.findEvent(eventId));
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> listEvents(@RequestParam("account") String accountId) {
        return ResponseEntity.ok(eventService.findEventsByAccount(accountId));
    }
}
