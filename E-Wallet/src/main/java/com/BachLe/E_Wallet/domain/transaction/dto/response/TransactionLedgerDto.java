package com.BachLe.E_Wallet.domain.transaction.dto.response;

import com.BachLe.E_Wallet.domain.transaction.entity.Transaction;
import com.BachLe.E_Wallet.domain.transaction.entity.TransactionLedger;
import jakarta.persistence.*;
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
