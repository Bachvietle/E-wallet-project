package com.BachLe.ewallet;

import com.BachLe.ewallet.domain.auth.entity.CustomUserDetails;
import com.BachLe.ewallet.domain.transaction.dto.request.TransferRequest;
import com.BachLe.ewallet.domain.transaction.service.TransactionService;
import com.BachLe.ewallet.domain.wallet.entity.Wallet;
import com.BachLe.ewallet.domain.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class TransactionConcurrencyTest {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private WalletRepository walletRepository;

    @Test
    public void testConcurrentTransfers() throws InterruptedException {
        // 1. SETUP DỮ LIỆU
        // tạo thủ công 2 user và 2 wallet trong DB,
        // Sau đó điền 2 UUID wallet thực tế vào đây
        UUID senderWalletId = UUID.fromString("f67feb62-0dbf-46e9-9e51-8a9557994cd2");
        UUID receiverWalletId = UUID.fromString("9395d6fc-0130-46d0-8116-cfa43fca4aea");


        CustomUserDetails mockUser = new CustomUserDetails(
                UUID.fromString("f71861d5-f47e-42d3-b9bc-124f0181a683"), // Chú ý: ID của USER, không phải của WALLET
                UUID.fromString("f67feb62-0dbf-46e9-9e51-8a9557994cd2"),
                "vietb",
                "password",
                List.of()
        );

        // Bơm vào Security Context
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Thiết lập ThreadPool
        int numberOfThreads = 100;
        ExecutorService threadPool = Executors.newFixedThreadPool(numberOfThreads);

        ExecutorService executorService = new DelegatingSecurityContextExecutorService(threadPool);

        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // 2. BẮN 100 REQUEST CÙNG LÚC
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {

                    // tạo request
                    TransferRequest request = new TransferRequest();
                    request.setReceiverWalletId(receiverWalletId);
                    request.setAmount(new BigDecimal("1000")); // Chuyển 1000đ mỗi lần
                    request.setMessage("Test race condition");

                    // chạy hàm service
                    transactionService.executeTransfer(request, senderWalletId, UUID.randomUUID().toString());
                } catch (Exception e) {
                    System.err.println("Giao dịch lỗi: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        // Đợi cả 100 thread chạy xong
        latch.await();
        executorService.shutdown();

        // 3. KIỂM TRA KẾT QUẢ
        Wallet sender = walletRepository.findById(senderWalletId).orElseThrow();
        Wallet receiver = walletRepository.findById(receiverWalletId).orElseThrow();
        System.out.println("Số dư Ví Gửi: " + sender.getBalance());
        System.out.println("Số dư Ví Nhận: " + receiver.getBalance());

        // Nếu ban đầu Ví Gửi có 1.000.000đ, bắn 100 phát mỗi phát 1000đ
        // -> Kết quả đúng phải là đúng 900.000đ. Không được sai lệch 1 đồng.
        // assertEquals(new BigDecimal("900000.0000"), sender.getBalance());
    }
}
