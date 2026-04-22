package com.BachLe.E_Wallet.domain.transaction.service;

import com.BachLe.E_Wallet.common.security.CustomUserDetails;
import com.BachLe.E_Wallet.domain.transaction.dto.request.TransferRequest;
import com.BachLe.E_Wallet.common.entity.Fee;
import com.BachLe.E_Wallet.domain.transaction.dto.response.TransferResponse;
import com.BachLe.E_Wallet.domain.transaction.entity.Transaction;
import com.BachLe.E_Wallet.domain.transaction.entity.TransactionLedger;
import com.BachLe.E_Wallet.domain.transaction.repository.TransactionLedgerRepository;
import com.BachLe.E_Wallet.domain.transaction.repository.TransactionRepository;
import com.BachLe.E_Wallet.domain.wallet.entity.Wallet;
import com.BachLe.E_Wallet.domain.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionLedgerRepository transactionLedgerRepository;

    @Transactional
    public void executeTransfer(TransferRequest request, String idempotencyKey){

        /// 1. Lấy senderId từ accessToken -> senderWalletId
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UUID senderWalletId = userDetails.getWalletId();

        UUID receiverWalletId = request.getReceiverWalletId();

        BigDecimal amount = request.getAmount();

        BigDecimal fee = Fee.TRANSFER.calculateFeeAmount(amount);


        /// 2. Ktra số tiền gửi, id người nhận != người gửi
        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new RuntimeException("Số tiền gửi phải > 0");
        }

        if (senderWalletId.equals(receiverWalletId)){
            throw new RuntimeException("Không được tự gửi tiền cho mình");
        }


        /// 3. Khóa 2 ví, luôn luôn khóa ví có id nhỏ hơn trước
        UUID firstIdLock = senderWalletId.compareTo(receiverWalletId) <= 0 ? senderWalletId : receiverWalletId;

        UUID secondIdLock = senderWalletId.compareTo(receiverWalletId) <= 0 ? receiverWalletId : senderWalletId;


        Wallet firstWallet = walletRepository.getWalletForUpdate(firstIdLock)
                .orElseThrow();

        Wallet secondWallet = walletRepository.getWalletForUpdate(secondIdLock)
                .orElseThrow();


        /// 4. Xác định lại ví gửi, ví nhận (ko cần getWalletById(senderWalletId) nữa vì như thế gây lãng phí, ở trên đã lấy cả 2 ví r)
        Wallet senderWallet = firstWallet.getId().equals(senderWalletId) ? firstWallet : secondWallet;

        Wallet receiverWallet = secondWallet.getId().equals(receiverWalletId) ? secondWallet : firstWallet;

        if (senderWallet.getStatus() != Wallet.WalletStatus.ACTIVE || receiverWallet.getStatus() != Wallet.WalletStatus.ACTIVE){
            throw new RuntimeException("Ví bị khóa");
        }

        /// 5. Tính toán số dư (logic Before/After)
        BigDecimal senderBalanceBefore = senderWallet.getBalance();
        BigDecimal senderBalanceAfter = senderWallet.debit(amount, fee);

        BigDecimal receiverBalanceBefore = receiverWallet.getBalance();
        BigDecimal receiverBalanceAfter = receiverWallet.credit(amount);

        // Lưu ví (Thực chất JPA sẽ tự update khi commit transaction, nhưng gọi save cho tường minh)
        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        /// 6. Tạo record Transaction, TransactionLedger lưu vào db

        Transaction newTransaction = Transaction.builder()
                .idempotencyKey(idempotencyKey)
                .amount(amount)
                .fee(fee)
                .transactionType(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.SUCCESS)
                .message(request.getMessage())
                .build();


        transactionRepository.save(newTransaction);


        TransactionLedger senderLedger = TransactionLedger.builder()
                .transaction(newTransaction)
                .amount(amount)
                .walletId(senderWalletId)
                .direction(TransactionLedger.Direction.OUT)
                .balanceBefore(senderBalanceBefore)
                .balanceAfter(senderBalanceAfter)
                .build();


        TransactionLedger receiverLedger = TransactionLedger.builder()
                .transaction(newTransaction)
                .amount(amount)
                .walletId(receiverWalletId)
                .direction(TransactionLedger.Direction.IN)
                .balanceBefore(receiverBalanceBefore)
                .balanceAfter(receiverBalanceAfter)
                .build();

        transactionLedgerRepository.saveAll(List.of(senderLedger, receiverLedger));
    }

}
