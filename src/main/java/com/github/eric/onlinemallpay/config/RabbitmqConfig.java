package com.github.eric.onlinemallpay.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class RabbitmqConfig {

    final public static String PAY_NOTIFY_QUEUE = "payNotify";
    final public static String PAY_NOTIFY_DEAD_QUEUE = "payNotifyDead";

    final public static String TEST_ROUTING_KEY_HAHA = "routingKey.haha";
    final public static String TEST_ROUTING_KEY_DEAD = "routingKey.dead";

    final public static String TEST_EXCHANGE_HAHA = "exchange.haha";
    final public static String TEST_EXCHANGE_DEAD = "exchange.dead";

    final public static String TEST_BIND_HAHA = "bind.haha";
    final public static String TEST_BIND_DEAD = "bind.dead";

    /**
     * 声明处理正常消息的交换机
     */
    @Bean(TEST_EXCHANGE_HAHA)
    DirectExchange hahaExchange() {
        return ExchangeBuilder.directExchange(TEST_EXCHANGE_HAHA).durable(true).build();
    }

    /**
     * 声明存储正常消息的队列，并设置死亡交换机和对应的路由key
     */
    @Bean(PAY_NOTIFY_QUEUE)
    public Queue queue1() {
        Map<String,Object> map = new HashMap<>();
        map.put("x-dead-letter-exchange",TEST_EXCHANGE_DEAD);
        map.put("x-dead-letter-routing-key",TEST_ROUTING_KEY_DEAD);
        return QueueBuilder.durable(PAY_NOTIFY_QUEUE).withArguments(map).build();
    }

    /**
     * 绑定正常队列和交换机
     */
    @Bean(TEST_BIND_HAHA)
    Binding bindingExchangeMessages(@Qualifier(PAY_NOTIFY_QUEUE) Queue queue, @Qualifier(TEST_EXCHANGE_HAHA) DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(TEST_ROUTING_KEY_HAHA);
    }

    /**
     * 声明死信交换机
     */
    @Bean(TEST_EXCHANGE_DEAD)
    DirectExchange deadExchange() {
        return ExchangeBuilder.directExchange(TEST_EXCHANGE_DEAD).durable(true).build();
    }

    /**
     * 声明死信队列
     */
    @Bean(PAY_NOTIFY_DEAD_QUEUE)
    public Queue queue2() {
        return QueueBuilder.durable(PAY_NOTIFY_DEAD_QUEUE).build();
    }

    /**
     * 绑定死信队列和交换机
     */
    @Bean(TEST_BIND_DEAD)
    Binding bindingExchangeMessage(@Qualifier(PAY_NOTIFY_DEAD_QUEUE)Queue queue, @Qualifier(TEST_EXCHANGE_DEAD)DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(TEST_ROUTING_KEY_DEAD);
    }

    /**
     * 配置生产者发送消息到达RabbitMQ通知
     */
    @Bean
    public RabbitTemplate.ConfirmCallback confirmCallback() {
        return new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData,
                                boolean ack, String cause) {
                if (ack) {
                    System.out.println("发送者爸爸已经收到确认" + ack);
//                    log.info("发送者爸爸已经收到确认，correlationData={} ,ack={}, cause={}", correlationData.getId(), ack, cause);
                } else {
                    System.out.println("消息发送异常!");
//                    log.error("消息发送异常!");
                }
            }
        };
    }

    /**
     * 配置生产者发送消息到达RabbitMQ指定队列失败通知
     */
    @Bean
    public RabbitTemplate.ReturnsCallback returnCallback() {
        return new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returned) {
                System.out.println("消息未进入队列" + new String(returned.getMessage().getBody()));
//                log.info("消息未进入队列"+new String(returned.getMessage().getBody()));
            }
        };
    }

}
