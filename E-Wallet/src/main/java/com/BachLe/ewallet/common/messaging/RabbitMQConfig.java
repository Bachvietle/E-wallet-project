package com.BachLe.ewallet.common.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;

import java.util.concurrent.Executor;


@Configuration
public class RabbitMQConfig {

    // 1. Định nghĩa tên Exchange, Queue và Routing Key bằng các hằng số cố định
    public static final String WALLET_EXCHANGE = "x.wallet.event";
    public static final String NOTIFICATION_TRANSFER_QUEUE = "q.notification.transfer";
    public static final String NOTIFICATION_WITHDRAW_QUEUE = "q.notification.WITHDRAW";
    public static final String TRANSFER_SUCCESS_ROUTING_KEY = "wallet.transfer.success";

    public static final String EMAIL_EXCHANGE = "x.email.event";
    public static final String EMAIL_QUEUE = "q.email";
    public static final String EMAIL_ROUTING_KEY = "email.#";


    // 2. Khai báo Khởi tạo Queue
    @Bean
    public Queue notificationTransferQueue(){
        return QueueBuilder.durable(NOTIFICATION_TRANSFER_QUEUE).build();
    }

    @Bean
    public Queue emailQueue(){
        return QueueBuilder.durable(EMAIL_QUEUE).build();
    }

    // 3. Khai báo Khởi tạo Topic Exchange
    @Bean
    public TopicExchange walletExchange(){
        return ExchangeBuilder.topicExchange(WALLET_EXCHANGE).build();
    }

    @Bean
    public TopicExchange emailExchange(){return ExchangeBuilder.topicExchange(EMAIL_EXCHANGE).build();}

    // 4. Khai báo Binding: Nối dây từ Queue vào Exchange thông qua Routing Key tương ứng
    @Bean
    public Binding bindingNotificationTransfer(Queue notificationTransferQueue, TopicExchange walletExchange){
        return BindingBuilder.bind(notificationTransferQueue)
                .to(walletExchange)
                .with(TRANSFER_SUCCESS_ROUTING_KEY);
    }

    @Bean
    public Binding bindingEmailSend(Queue emailQueue, TopicExchange emailExchange){
        return BindingBuilder.bind(emailQueue)
                .to(emailExchange)
                .with(EMAIL_ROUTING_KEY);
    }


    // 5. Cấu hình MessageConverter thành JSON
    // Tận dụng bài học xương máu từ Redis: Ép dữ liệu sang JSON thay vì dùng mã hóa nhị phân mặc định của Java
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


    // 6. Factory kết nối tới RabbitMQ dành cho từng tác vụ
    @Bean
    public SimpleRabbitListenerContainerFactory notificationListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jackson2JsonMessageConverter,
            @Qualifier("notificationListenerTaskExecutor") Executor taskExecutor) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter);

        factory.setTaskExecutor(taskExecutor);

        // Số lượng worker tối thiểu và tối đa chạy song song để nhặt message từ queue
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(5);

        return factory;
    }


    @Bean
    public SimpleRabbitListenerContainerFactory emailListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jackson2JsonMessageConverter,
            @Qualifier("emailListenerTaskExecutor") Executor taskExecutor) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter);

        factory.setTaskExecutor(taskExecutor);

        // Số lượng worker tối thiểu và tối đa chạy song song để nhặt message từ queue
        factory.setConcurrentConsumers(10);
        factory.setMaxConcurrentConsumers(10);

        return factory;
    }
}
