package com.example.accountservice.service;

import com.example.accountservice.dto.AccountResponse;
import com.example.accountservice.dto.BalanceResponse;
import com.example.accountservice.dto.TransactionDetails;
import com.example.accountservice.dto.TransactionRequest;
import com.example.accountservice.dto.TransactionResponse;
import com.example.accountservice.entity.AccountEntity;
import com.example.accountservice.entity.TransactionEntity;
import com.example.accountservice.exception.AccountNotFoundException;
import com.example.accountservice.exception.DuplicateTransactionException;
import com.example.accountservice.repository.AccountRepository;
import com.example.accountservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountMetrics accountMetrics;

    @Transactional
    public TransactionResponse recordTransaction(String accountId, TransactionRequest request) {
        if (transactionRepository.findByEventId(request.getEventId()).isPresent()) {
            throw new DuplicateTransactionException("Duplicate transaction eventId: " + request.getEventId());
        }

        AccountEntity account = accountRepository.findById(accountId)
                .orElse(new AccountEntity(accountId, BigDecimal.ZERO));

        BigDecimal updatedBalance = calculateBalance(account.getBalance(), request);
        account.setBalance(updatedBalance);
        accountRepository.save(account);

        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setEventId(request.getEventId());
        transactionEntity.setAccountId(accountId);
        transactionEntity.setType(request.getType().name());
        transactionEntity.setAmount(request.getAmount());
        transactionEntity.setCurrency(request.getCurrency());
        transactionEntity.setEventTimestamp(request.getEventTimestamp());
        transactionRepository.save(transactionEntity);

        accountMetrics.incrementTransactionCount();
        log.info("Recorded transaction {} for account {}", request.getEventId(), accountId);

        return TransactionResponse.builder()
                .eventId(request.getEventId())
                .accountId(accountId)
                .type(request.getType())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .eventTimestamp(request.getEventTimestamp())
                .balance(updatedBalance)
                .createdAt(transactionEntity.getCreatedAt())
                .build();
    }

    public BalanceResponse getBalance(String accountId) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));
        return BalanceResponse.builder()
                .accountId(accountId)
                .balance(account.getBalance())
                .build();
    }

    public AccountResponse getAccount(String accountId) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

        List<TransactionDetails> transactions = transactionRepository.findByAccountIdOrderByEventTimestampAsc(accountId).stream()
                .map(this::toTransactionDetails)
                .collect(Collectors.toList());

        return AccountResponse.builder()
                .accountId(accountId)
                .balance(account.getBalance())
                .transactions(transactions)
                .build();
    }

    private BigDecimal calculateBalance(BigDecimal currentBalance, TransactionRequest request) {
        BigDecimal adjustment = request.getType() == com.example.accountservice.dto.TransactionType.DEPOSIT
                ? request.getAmount()
                : request.getAmount().negate();
        return currentBalance.add(adjustment);
    }

    private TransactionDetails toTransactionDetails(TransactionEntity entity) {
        return TransactionDetails.builder()
                .eventId(entity.getEventId())
                .type(Enum.valueOf(com.example.accountservice.dto.TransactionType.class, entity.getType()))
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .eventTimestamp(entity.getEventTimestamp())
                .build();
    }
}
