package com.BachLe.E_Wallet.domain.transaction.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Data
public class TransferResponse {

    UUID senderWalletId;

    UUID receiverWalletId;

    BigDecimal amount;

    BigDecimal senderBalanceAfter;

    BigDecimal receiverBalanceAfter;

    String message;

    UUID transactionCode;
}
