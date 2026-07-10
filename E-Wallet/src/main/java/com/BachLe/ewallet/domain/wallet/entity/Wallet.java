package com.BachLe.ewallet.domain.wallet.entity;

import com.BachLe.ewallet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallets")
public class Wallet extends BaseEntity {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Chuẩn DDD: Liên kết sang module User bằng ID (Không dùng @OneToOne)
    @Setter
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    // precision = 19, scale = 4 khớp 100% với NUMERIC(19,4) trong DB
    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Setter
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "VND";

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private WalletStatus status = WalletStatus.ACTIVE;

    public enum WalletStatus {
        ACTIVE, LOCKED
    }

    public BigDecimal debit(BigDecimal amount, BigDecimal feeAmount){

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền rút phải lớn hơn 0");
        }

        BigDecimal totalDebit = amount.add(feeAmount);
        if (this.getBalance().compareTo(totalDebit) < 0) {
            throw new RuntimeException("Số dư không đủ để thực hiện giao dịch");
        }

        this.balance = this.balance.subtract(totalDebit);

        return this.balance;
    }

    public BigDecimal credit(BigDecimal amount){
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0");
        }

       this.balance = this.getBalance().add(amount);

        return this.balance;
    }

}
