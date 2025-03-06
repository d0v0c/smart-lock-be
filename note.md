## App -> 后端 发送数据

接口文档

http://63.32.52.62/swagger-ui/index.html

### 查询设备

```http request
http://63.32.52.62/api/device
```

### 请求生成密码

```http request
http://63.32.52.62/api/code
body:
{
    "deviceId": 10002,
    "validFrom": "2025-03-22T00:00+08:00",
    "validTo": "2027-03-05T00:30+00:00"
}
```

## ESP32 -> Broker 发送数据

192.109.228.41:1883

username: alice

password: alice123456

### 更新状态

device/lock

```json
{
  "deviceId": "10002",
  "isLocked": true
}
```

### 确认收到密码

device/lock/code

```json
{
  "deviceId": "10002",
  "codeId": "${收到的codeId}",
  "code": "${收到的code}"
}
```

### 发送警报

device/lock/alert

```json
{
  "deviceId": "10001",
  "type": "MOTOR"
}
```

## 后端 -> ESP32(Broker) 发送数据

### 生成的密码

server/lock/code

```json
{
  "code": "645941",
  "codeId": 1893389214167859200,
  "deviceId": 10002,
  "validFrom": "2025-03-22T00:00+08:00",
  "validTo": "2026-02-22T00:00+08:00"
}
```

```mermaid
sequenceDiagram
    participant C as 客户端
    participant S as 服务器
    Note over S: Config：<br>设置应用前缀 /app<br>设置代理前缀：/topic<br>注册端点 /websocket
%% 建立连接阶段
    C ->> S: ws://<host>/websocket
    S -->> C: 完成 WebSocket 握手
    C ->> S: STOMP CONNECT
    S -->> C: STOMP CONNECTED
    Note over S: SessionConnectedEvent<br>连接建立成功
%% 订阅与注册阶段
    C ->> S: STOMP SUBSCRIBE /topic/public
    C ->> S: STOMP SEND /app/chat.register
    S ->> S: /app 消息被路由到<br>@MessageMapping 的 Controller
    Note over S: Controller<br>把用户名存到 sessionAttributes
    S -->> C: @SendTo("/topic/public")
%% 聊天消息发送阶段
    C ->> S: STOMP SEND /app/chat.send 聊天内容
    S ->> S: /app 消息被路由到<br>@MessageMapping 的 Controller
    Note over S: Controller 处理聊天消息
    S -->> C: @SendTo("/topic/public")
%% 断开连接阶段
    C ->> S: WebSocket 连接断开
    S ->> S: SessionDisconnectEvent<br>执行 WebSocketEventListener.handleWebSocketDisconnectListener
    S -->> C: 广播离线消息至 /topic/public

```
