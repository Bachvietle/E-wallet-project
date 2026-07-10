package com.BachLe.ewallet.domain.transaction.dto.response;

import com.BachLe.ewallet.domain.transaction.entity.Transaction;
import com.BachLe.ewallet.domain.transaction.entity.TransactionLedger;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TransactionLedgerDto {

    private UUID transactionCode;

    private UUID walletId;

    private TransactionLedger.Direction direction;

    private BigDecimal amount;

    private BigDecimal balanceAfter;

    private String message;

    private Transaction.TransactionType transactionType;

    private LocalDateTime createdAt;
}
