package com.example.eventgateway.controller;

import com.example.eventgateway.service.AccountServiceClient;
import com.example.eventgateway.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountServiceClient accountServiceClient;

    @Autowired
    private EventRepository eventRepository;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    void shouldSubmitEventAndRetrieveEvent() throws Exception {
        given(accountServiceClient.postTransaction(anyString(), any(), anyString()))
                .willReturn(java.util.concurrent.CompletableFuture.completedFuture(null));

        String eventJson = "{" +
                "\"eventId\":\"evt-abc\"," +
                "\"accountId\":\"acct-01\"," +
                "\"type\":\"DEPOSIT\"," +
                "\"amount\":100.00," +
                "\"currency\":\"USD\"," +
                "\"eventTimestamp\":\"2026-06-16T12:00:00Z\"}";

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Trace-Id"))
                .andExpect(jsonPath("$.eventId").value("evt-abc"));

        mockMvc.perform(get("/events/evt-abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("acct-01"));
    }

    @Test
    void shouldReturnSortedEventsByAccount() throws Exception {
        given(accountServiceClient.postTransaction(anyString(), any(), anyString()))
                .willReturn(java.util.concurrent.CompletableFuture.completedFuture(null));

        eventRepository.saveAll(java.util.List.of(
                new com.example.eventgateway.entity.EventEntity() {{
                    setEventId("evt-100");
                    setAccountId("acct-02");
                    setType("DEPOSIT");
                    setAmount(BigDecimal.valueOf(10));
                    setCurrency("USD");
                    setEventTimestamp(Instant.parse("2026-06-16T12:00:00Z"));
                    setCreatedAt(Instant.now());
                }},
                new com.example.eventgateway.entity.EventEntity() {{
                    setEventId("evt-101");
                    setAccountId("acct-02");
                    setType("DEPOSIT");
                    setAmount(BigDecimal.valueOf(20));
                    setCurrency("USD");
                    setEventTimestamp(Instant.parse("2026-06-15T12:00:00Z"));
                    setCreatedAt(Instant.now());
                }}
        ));

        mockMvc.perform(get("/events").param("account", "acct-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value("evt-101"))
                .andExpect(jsonPath("$[1].eventId").value("evt-100"));
    }
}
