package com.BachLe.ewallet.domain.transaction.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransferRequest {

    @NotNull
    UUID receiverWalletId;

    @NotNull
    @Positive
    BigDecimal amount;

    @NotBlank
    String message;
}
