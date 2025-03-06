package ie.tcd.scss.smartdoorlockbe.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * @author xylingying
 * @date 2025-03-06 11:25
 * @description: 配置WebSocket
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker   //启用 Spring 的 WebSocket 消息代理
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    /**
     * 提供 STOMP 端点，Flutter 可直接通过 ws://<host>/websocket 进行连接
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //registry.addEndpoint("/websocket").withSockJS();//为客户端提供兼容性支持，即使浏览器原生不支持 WebSocket，也可以使用 SockJS 模拟连接
        registry.addEndpoint("/websocket").setAllowedOrigins("*");
        log.info("注册 STOMP 端点");
    }

    /**
     * 配置消息代理以使用带有指定目标前缀的简单代理。
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        log.info("配置消息代理topic+app");
        //启用一个简单的内存消息代理，用于处理以 "/topic" 开头的消息目的地（Topic，即广播消息）。这部分消息会直接由代理转发给所有订阅此主题的客户端。
        registry.enableSimpleBroker("/queue");
        //指定所有以 "/app" 开头的消息都将被路由到带有 @MessageMapping 注解的方法进行处理，而不是直接发送到消息代理。这种做法可区分应用程序内部的消息处理和消息代理的广播。
        registry.setUserDestinationPrefix("/user");
    }
}
