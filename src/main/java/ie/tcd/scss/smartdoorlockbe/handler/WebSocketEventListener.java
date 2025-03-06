package ie.tcd.scss.smartdoorlockbe.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * @author xylingying
 * @date 2025-03-06 11:32
 * @description: 监听ws连接建立或断开
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * WebSocket连接建立时触发
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        if (headerAccessor.getUser() != null) {
            String username = headerAccessor.getUser().getName();
            System.out.println("WebSocket 连接成功，用户：" + username);
        } else {
            System.out.println("WebSocket 连接时未获取到用户信息");
        }
    }

    /**
     * WebSocket 断开连接时触发
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        if (headerAccessor.getUser() != null) {
            String username = headerAccessor.getUser().getName();
            System.out.println("WebSocket 断开成功，用户：" + username);
        } else {
            System.out.println("WebSocket 断开时未获取到用户信息");
        }
    }
}
