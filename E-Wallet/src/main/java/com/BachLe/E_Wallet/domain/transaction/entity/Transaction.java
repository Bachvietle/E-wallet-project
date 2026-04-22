package com.BachLe.E_Wallet.domain.transaction.entity;

import com.BachLe.E_Wallet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;


@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")

public class Transaction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private TransactionType transactionType;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(length = 255)
    private String message;

    public enum TransactionType {
        TRANSFER, DEPOSIT, WITHDRAW
    }

    public enum TransactionStatus {
        PENDING, SUCCESS, FAILED
    }

}
