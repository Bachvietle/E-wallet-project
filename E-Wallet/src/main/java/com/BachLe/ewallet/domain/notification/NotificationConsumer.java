package com.BachLe.ewallet.domain.notification;

import com.BachLe.ewallet.common.messaging.RabbitMQConfig;
import com.BachLe.ewallet.domain.transaction.event.TransferSuccessEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationConsumer {

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_TRANSFER_QUEUE, containerFactory = "notificationListenerContainerFactory")
    public void processTransferNotification(TransferSuccessEvent event) {
        try {
            log.info("Có giao dịch chuyển tiền mới: Mã GD {}", event.getTransactionCode());
            log.info("Gửi từ: {} | Đến: {} | Số tiền: {}",
                    event.getSenderWalletId(),
                    event.getReceiverWalletId(),
                    event.getAmount());

            // 1. Giả lập một tác vụ tốn thời gian (Gọi API sang hệ thống thứ 3, Gửi Email, Push Notification)
            log.info("Đang kết nối tới SMTP Server để gửi Email thông báo...");
            Thread.sleep(3000); // Tạm dừng 3 giây để giả lập độ trễ mạng

            // 2. Hoàn thành
            log.info("Đã gửi thông báo thành công cho ví nhận: {}\n", event.getReceiverWalletId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Tiến trình Worker bị gián đoạn", e);
        } catch (Exception e) {
            // CỰC KỲ QUAN TRỌNG: Bắt mọi Exception ở đây.
            // Nếu không bắt, Spring sẽ mặc định ném lỗi và tự động requeue (đẩy tin nhắn lại vào hàng đợi),
            // dẫn đến vòng lặp vô tận (Poison Pill) làm kẹt cứng CPU của bạn.
            throw new RuntimeException("");
        }
    }
}
