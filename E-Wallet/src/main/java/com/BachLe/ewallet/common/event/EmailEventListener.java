package com.BachLe.ewallet.common.event;


import com.BachLe.ewallet.common.messaging.RabbitMQConfig;
import com.BachLe.ewallet.domain.auth.event.UserRegisterEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEventListener {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlerEmailEvent(UserRegisterEvent event){

        try {

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    event
            );

        } catch (Exception e){
            log.error("Lỗi khi đẩy event lên RabbitMQ: {}", e.getMessage(), e);
        }
    }


}
