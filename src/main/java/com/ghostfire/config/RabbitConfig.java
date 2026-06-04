package com.ghostfire.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String VIEW_COUNT_QUEUE = "q.view.count";
    public static final String WALLET_LOG_QUEUE = "q.wallet.log";
    public static final String EXCHANGE = "x.ghostfire";
    public static final String VIEW_COUNT_KEY = "view.count";
    public static final String WALLET_LOG_KEY = "wallet.log";

    @Bean
    public TopicExchange ghostfireExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue viewCountQueue() {
        return QueueBuilder.durable(VIEW_COUNT_QUEUE).build();
    }

    @Bean
    public Queue walletLogQueue() {
        return QueueBuilder.durable(WALLET_LOG_QUEUE).build();
    }

    @Bean
    public Binding viewCountBinding(Queue viewCountQueue, TopicExchange ghostfireExchange) {
        return BindingBuilder.bind(viewCountQueue).to(ghostfireExchange).with(VIEW_COUNT_KEY);
    }

    @Bean
    public Binding walletLogBinding(Queue walletLogQueue, TopicExchange ghostfireExchange) {
        return BindingBuilder.bind(walletLogQueue).to(ghostfireExchange).with(WALLET_LOG_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
