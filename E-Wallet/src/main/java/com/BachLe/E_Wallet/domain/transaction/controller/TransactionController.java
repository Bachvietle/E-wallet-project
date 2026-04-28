package com.BachLe.E_Wallet.domain.transaction.controller;


import com.BachLe.E_Wallet.common.dto.ApiResponse;
import com.BachLe.E_Wallet.domain.transaction.dto.request.TransferRequest;
import com.BachLe.E_Wallet.domain.transaction.dto.response.TransactionLedgerDto;
import com.BachLe.E_Wallet.domain.transaction.dto.response.TransferResponse;
import com.BachLe.E_Wallet.domain.transaction.entity.TransactionLedger;
import com.BachLe.E_Wallet.domain.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> executeTransfer(@RequestBody TransferRequest request, @RequestHeader("Idempotency-Key") String idempotencyKey){

        TransferResponse data = transactionService.executeTransfer(request, idempotencyKey);

        return new ResponseEntity<>(ApiResponse.success("Giao dịch thành công", data), HttpStatus.OK);
    }

}
