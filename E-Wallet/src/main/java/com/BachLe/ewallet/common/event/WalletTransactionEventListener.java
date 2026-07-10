package com.BachLe.ewallet.common.event;

import com.BachLe.ewallet.common.messaging.RabbitMQConfig;
import com.BachLe.ewallet.domain.transaction.event.TransferSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor

// "Người trung gian" (Event Listener -> RabbitMQ Publisher)
public class WalletTransactionEventListener {

    private final RabbitTemplate rabbitTemplate;

    // Hàm này CHỈ CHẠY khi DB đã commit thành công
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransferSuccessEvent(TransferSuccessEvent event){

        try{
            log.info("Đẩy event sang RabbitMQ: " + event);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.WALLET_EXCHANGE,
                    RabbitMQConfig.TRANSFER_SUCCESS_ROUTING_KEY,
                    event
            );

            log.info("Đã đẩy thành công event lên RabbitMQ!");
        } catch (Exception e){
            // Try-catch ở đây cực kỳ quan trọng.
            // Nếu RabbitMQ chết, luồng này ném lỗi, nhưng KHÔNG làm rollback DB của hàm chuyển tiền gốc.
            log.error("Lỗi khi đẩy event lên RabbitMQ: {}", e.getMessage(), e);
        }
    }
}
