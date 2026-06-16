package com.example.eventgateway.client;

import com.example.eventgateway.dto.TransactionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "account-service", url = "${account.service.url}")
public interface AccountClient {

    @PostMapping(value = "/accounts/{accountId}/transactions", consumes = MediaType.APPLICATION_JSON_VALUE)
    void postTransaction(
            @PathVariable("accountId") String accountId,
            @RequestBody TransactionRequest transactionRequest,
            @RequestHeader("X-Trace-Id") String traceId
    );
}
