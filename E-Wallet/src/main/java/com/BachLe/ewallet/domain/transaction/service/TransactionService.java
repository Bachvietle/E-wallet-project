package com.BachLe.ewallet.domain.transaction.service;

import com.BachLe.ewallet.common.annotation.RateLimit;
import com.BachLe.ewallet.common.cache.CacheHelper;
import com.BachLe.ewallet.domain.transaction.dto.request.TransferRequest;
import com.BachLe.ewallet.domain.transaction.entity.Fee;
import com.BachLe.ewallet.domain.transaction.dto.response.TransactionLedgerDto;
import com.BachLe.ewallet.domain.transaction.dto.response.TransferResponse;
import com.BachLe.ewallet.domain.transaction.entity.Transaction;
import com.BachLe.ewallet.domain.transaction.entity.TransactionLedger;
import com.BachLe.ewallet.domain.transaction.event.TransferSuccessEvent;
import com.BachLe.ewallet.domain.transaction.repository.TransactionLedgerRepository;
import com.BachLe.ewallet.domain.transaction.repository.TransactionRepository;
import com.BachLe.ewallet.domain.wallet.entity.Wallet;
import com.BachLe.ewallet.domain.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@RateLimit(requests = 5, duration = 5)
public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionLedgerRepository transactionLedgerRepository;
    private final CacheHelper cacheHelper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public TransferResponse executeTransfer(TransferRequest request, UUID senderWalletId, String idempotencyKey) {

        UUID receiverWalletId = request.getReceiverWalletId();

        BigDecimal amount = request.getAmount();

        BigDecimal fee = Fee.TRANSFER.calculateFeeAmount(amount);


        /// 2. Ktra số tiền gửi, id người nhận != người gửi
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền gửi phải > 0");
        }

        if (senderWalletId.equals(receiverWalletId)) {
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

        if (senderWallet.getStatus() != Wallet.WalletStatus.ACTIVE || receiverWallet.getStatus() != Wallet.WalletStatus.ACTIVE) {
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

        cacheHelper.evictAfterCommit("walletBalance", senderWalletId);
        cacheHelper.evictAfterCommit("walletBalance", receiverWalletId);

        TransferSuccessEvent event = TransferSuccessEvent.builder()
                .transactionCode(newTransaction.getId())
                .senderWalletId(senderWalletId)
                .receiverWalletId(receiverWalletId)
                .amount(amount)
                .message(request.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        applicationEventPublisher.publishEvent(event);

        return TransferResponse.builder()
                .senderWalletId(senderWalletId)
                .receiverWalletId(receiverWalletId)
                .amount(amount)
                .message(request.getMessage())
                .senderBalanceAfter(senderBalanceAfter)
                .receiverBalanceAfter(receiverBalanceAfter)
                .transactionCode(newTransaction.getId())
                .build();
    }

    public Page<TransactionLedgerDto> getTransactionHistory(UUID walletId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<TransactionLedger> transactionLedgerPage = transactionLedgerRepository.findTransactionLedgerByWalletId(walletId, pageable);

        return transactionLedgerPage.map(transactionLedger -> mapToDto(transactionLedger));

    }

    private TransactionLedgerDto mapToDto(TransactionLedger transactionLedger) {

        Transaction transaction = transactionLedger.getTransaction();

        return TransactionLedgerDto.builder()
                .transactionCode(transaction.getId())
                .walletId(transactionLedger.getWalletId())
                .direction(transactionLedger.getDirection())
                .amount(transactionLedger.getAmount())
                .balanceAfter(transactionLedger.getBalanceAfter())
                .message(transaction.getMessage())
                .transactionType(transaction.getTransactionType())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
