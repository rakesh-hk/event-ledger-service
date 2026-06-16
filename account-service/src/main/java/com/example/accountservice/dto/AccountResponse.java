package com.example.accountservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class AccountResponse {
    private String accountId;
    private BigDecimal balance;
    private List<TransactionDetails> transactions;

    @Data
    @Builder
    public static class TransactionDetails {
        private String eventId;
        private TransactionType type;
        private BigDecimal amount;
        private String currency;
        private Instant eventTimestamp;
    }
}
