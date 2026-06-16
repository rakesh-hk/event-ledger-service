package com.example.eventgateway.service;

import com.example.eventgateway.client.AccountClient;
import com.example.eventgateway.dto.TransactionRequest;
import com.example.eventgateway.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AccountServiceClient {

    private final AccountClient accountClient;

    @Retry(name = "accountService", fallbackMethod = "accountServiceFallback")
    @CircuitBreaker(name = "accountService", fallbackMethod = "accountServiceFallback")
    @TimeLimiter(name = "accountService", fallbackMethod = "accountServiceFallback")
    public CompletableFuture<Void> postTransaction(String accountId, TransactionRequest transactionRequest, String traceId) {
        return CompletableFuture.supplyAsync(() -> {
            accountClient.postTransaction(accountId, transactionRequest, traceId);
            return null;
        });
    }

    private CompletableFuture<Void> accountServiceFallback(String accountId,
                                                           TransactionRequest transactionRequest,
                                                           String traceId,
                                                           Throwable throwable) {
        return CompletableFuture.failedFuture(new ServiceUnavailableException("Unable to reach account service", throwable));
    }
}
