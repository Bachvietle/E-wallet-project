package com.BachLe.E_Wallet.common.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum Fee {

    TRANSFER("0.00"), // 0%
    DEPOSIT("0.00"),  // 0%
    WITHDRAW("0.01"); // 1%

    private final BigDecimal rate;

    Fee(String percentage) {
        // Luôn dùng new BigDecimal(String) thay vì new BigDecimal(double)
        this.rate = new BigDecimal(percentage);
    }

    public BigDecimal calculateFeeAmount(BigDecimal amount) {
        return amount.multiply(this.rate).setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal getRate() {
        return rate;
    }
}
