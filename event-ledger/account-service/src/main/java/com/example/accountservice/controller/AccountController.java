package com.example.accountservice.controller;

import com.example.accountservice.dto.AccountResponse;
import com.example.accountservice.dto.BalanceResponse;
import com.example.accountservice.dto.TransactionRequest;
import com.example.accountservice.dto.TransactionResponse;
import com.example.accountservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Validated
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/{accountId}/transactions")
    public ResponseEntity<TransactionResponse> submitTransaction(
            @PathVariable("accountId") String accountId,
            @Valid @RequestBody TransactionRequest request
    ) {
        TransactionResponse response = accountService.recordTransaction(accountId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable("accountId") String accountId) {
        return ResponseEntity.ok(accountService.getBalance(accountId));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable("accountId") String accountId) {
        return ResponseEntity.ok(accountService.getAccount(accountId));
    }
}
