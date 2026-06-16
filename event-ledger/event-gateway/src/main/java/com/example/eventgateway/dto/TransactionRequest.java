package com.example.eventgateway.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransactionRequest {

    @NotBlank(message = "eventId is required")
    private String eventId;

    @NotNull(message = "type is required")
    private EventType type;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    private String currency;

    @NotNull(message = "eventTimestamp is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant eventTimestamp;
}
