package com.example.accountservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRecordTransactionAndReturnBalance() throws Exception {
        String body = "{" +
                "\"eventId\":\"evt-xyz\"," +
                "\"type\":\"DEPOSIT\"," +
                "\"amount\":150.00," +
                "\"currency\":\"USD\"," +
                "\"eventTimestamp\":\"2026-06-16T12:00:00Z\"}";

        mockMvc.perform(post("/accounts/acct-100/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(150.00));

        mockMvc.perform(get("/accounts/acct-100/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(150.00));

        mockMvc.perform(get("/accounts/acct-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("acct-100"));
    }
}
