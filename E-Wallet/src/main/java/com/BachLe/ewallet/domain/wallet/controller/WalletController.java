package com.BachLe.ewallet.domain.wallet.controller;

import com.BachLe.ewallet.common.dto.ApiResponse;
import com.BachLe.ewallet.domain.auth.entity.CustomUserDetails;
import com.BachLe.ewallet.domain.transaction.dto.response.TransactionLedgerDto;
import com.BachLe.ewallet.domain.transaction.service.TransactionService;
import com.BachLe.ewallet.domain.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UUID walletId = userDetails.getWalletId();

        BigDecimal balance =  walletService.getBalance(walletId);

        ApiResponse<BigDecimal> response = ApiResponse.success("", balance);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/transaction-history")
    public ResponseEntity<ApiResponse> getTransactionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UUID walletId = userDetails.getWalletId();

        Page<TransactionLedgerDto> data = transactionService.getTransactionHistory(walletId, page, size);

        return new ResponseEntity<>(ApiResponse.success("Thành công", data), HttpStatus.OK);
    }
}
