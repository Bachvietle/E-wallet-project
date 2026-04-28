package com.BachLe.E_Wallet.domain.wallet.controller;

import com.BachLe.E_Wallet.common.dto.ApiResponse;
import com.BachLe.E_Wallet.domain.transaction.dto.response.TransactionLedgerDto;
import com.BachLe.E_Wallet.domain.transaction.service.TransactionService;
import com.BachLe.E_Wallet.domain.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    private final TransactionService transactionService;

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getBalance(){

        BigDecimal balance =  walletService.getBalance();

        ApiResponse<BigDecimal> response = ApiResponse.success("", balance);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<TransactionLedgerDto> data = transactionService.getTransactionHistory(page, size);

        return new ResponseEntity<>(ApiResponse.success("Thành công", data), HttpStatus.OK);
    }
}
