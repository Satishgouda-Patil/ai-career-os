package com.ai.career.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange:job.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue.fetch:job.fetch.queue}")
    private String fetchQueueName;

    @Value("${app.rabbitmq.queue.match:job.match.queue}")
    private String matchQueueName;

    @Value("${app.rabbitmq.queue.notify:notification.queue}")
    private String notifyQueueName;

    public static final String ROUTING_KEY_JOB_FETCHED = "job.fetched";
    public static final String ROUTING_KEY_PROFILE_UPDATED = "profile.updated";
    public static final String ROUTING_KEY_MATCH_FOUND = "match.found";

    @Bean
    public TopicExchange jobExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue fetchQueue() {
        return new Queue(fetchQueueName, true);
    }

    @Bean
    public Queue matchQueue() {
        return new Queue(matchQueueName, true);
    }

    @Bean
    public Queue notifyQueue() {
        return new Queue(notifyQueueName, true);
    }

    @Bean
    public Binding fetchBinding(Queue fetchQueue, TopicExchange jobExchange) {
        return BindingBuilder.bind(fetchQueue).to(jobExchange).with(ROUTING_KEY_JOB_FETCHED);
    }

    @Bean
    public Binding matchJobBinding(Queue matchQueue, TopicExchange jobExchange) {
        return BindingBuilder.bind(matchQueue).to(jobExchange).with(ROUTING_KEY_JOB_FETCHED);
    }

    @Bean
    public Binding matchProfileBinding(Queue matchQueue, TopicExchange jobExchange) {
        return BindingBuilder.bind(matchQueue).to(jobExchange).with(ROUTING_KEY_PROFILE_UPDATED);
    }

    @Bean
    public Binding notifyBinding(Queue notifyQueue, TopicExchange jobExchange) {
        return BindingBuilder.bind(notifyQueue).to(jobExchange).with(ROUTING_KEY_MATCH_FOUND);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
