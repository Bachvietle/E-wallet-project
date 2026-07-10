package com.BachLe.ewallet.domain.transaction.controller;


import com.BachLe.ewallet.common.annotation.Idempotent;
import com.BachLe.ewallet.common.annotation.RateLimit;
import com.BachLe.ewallet.common.dto.ApiResponse;
import com.BachLe.ewallet.domain.auth.entity.CustomUserDetails;
import com.BachLe.ewallet.domain.transaction.dto.request.TransferRequest;
import com.BachLe.ewallet.domain.transaction.dto.response.TransferResponse;
import com.BachLe.ewallet.domain.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
@RateLimit(requests = 2, duration = 10)
public class TransactionController {

    private final TransactionService transactionService;


    @Idempotent
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> executeTransfer(@RequestBody TransferRequest request, @RequestHeader("Idempotency-Key") String idempotencyKey){

        /// 1. Lấy senderId từ accessToken -> senderWalletId
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UUID senderWalletId = userDetails.getWalletId();

        TransferResponse data = transactionService.executeTransfer(request,senderWalletId, idempotencyKey);

        return new ResponseEntity<>(ApiResponse.success("Giao dịch thành công", data), HttpStatus.OK);
    }

}
