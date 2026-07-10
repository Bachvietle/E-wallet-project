package com.BachLe.ewallet.domain.transaction.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TransferSuccessEvent {
    private UUID transactionCode;
    private UUID senderWalletId;
    private UUID receiverWalletId;
    private BigDecimal amount;
    private String message;
    private LocalDateTime timestamp;
}
