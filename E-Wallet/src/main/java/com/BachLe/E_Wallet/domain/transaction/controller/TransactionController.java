package com.BachLe.E_Wallet.domain.transaction.controller;


import com.BachLe.E_Wallet.domain.transaction.dto.request.TransferRequest;
import com.BachLe.E_Wallet.domain.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public void executeTransfer(@RequestBody TransferRequest request, @RequestHeader  String idempotencyKey){


    }
}
