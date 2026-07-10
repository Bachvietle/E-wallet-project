package com.BachLe.ewallet.domain.notification;

import com.BachLe.ewallet.common.messaging.RabbitMQConfig;
import com.BachLe.ewallet.domain.auth.event.UserRegisterEvent;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE, containerFactory = "emailListenerContainerFactory")
    public void processSendRegisterLink(UserRegisterEvent event) {

        log.info("Đã gửi email");

        try {
            emailService.sendVerifyRegisterMail(event.getVerifyLink(), event.getUserMail());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }

}
