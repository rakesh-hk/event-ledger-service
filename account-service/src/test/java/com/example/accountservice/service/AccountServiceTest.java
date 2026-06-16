package com.example.accountservice.service;

import com.example.accountservice.dto.TransactionRequest;
import com.example.accountservice.dto.TransactionType;
import com.example.accountservice.entity.AccountEntity;
import com.example.accountservice.entity.TransactionEntity;
import com.example.accountservice.exception.DuplicateTransactionException;
import com.example.accountservice.repository.AccountRepository;
import com.example.accountservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountMetrics accountMetrics;

    @InjectMocks
    private AccountService accountService;

    private TransactionRequest request;

    @BeforeEach
    void setup() {
        request = new TransactionRequest();
        request.setEventId("evt-abc");
        request.setType(TransactionType.DEPOSIT);
        request.setAmount(BigDecimal.valueOf(100));
        request.setCurrency("USD");
        request.setEventTimestamp(Instant.parse("2026-06-16T12:00:00Z"));
    }

    @Test
    void shouldCreateAccountTransaction() {
        given(transactionRepository.findByEventId(request.getEventId())).willReturn(Optional.empty());
        given(accountRepository.findById("acct-01")).willReturn(Optional.empty());
        given(accountRepository.save(any(AccountEntity.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(transactionRepository.save(any(TransactionEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

        var response = accountService.recordTransaction("acct-01", request);

        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(response.getAccountId()).isEqualTo("acct-01");
        verify(accountMetrics).incrementTransactionCount();
    }

    @Test
    void shouldThrowDuplicateTransactionException() {
        given(transactionRepository.findByEventId(request.getEventId())).willReturn(Optional.of(new TransactionEntity()));

        assertThatThrownBy(() -> accountService.recordTransaction("acct-01", request))
                .isInstanceOf(DuplicateTransactionException.class);
    }
}
