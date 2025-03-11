## App -> 后端 发送数据

客户端接口文档

http://63.32.52.62/swagger-ui/index.html

### 注册

```http request
POST http://63.32.52.62/api/user/register
{
    "username": "alfa",
    "password": "123456"
}
```

### 登录

```http request
GET http://63.32.52.62/api/user/login
Authorization: Basic alfa:123456（传入的用户名密码需要经过base64编码）
```

### 查询设备

```http request
GET http://63.32.52.62/api/device
Authorization: Bearer JWT 
```

### 请求生成密码

```http request
POST http://63.32.52.62/api/code
Authorization: Bearer JWT
body:
{
    "deviceId": 10001,
    "validFrom": "2025-03-22T00:00+08:00",
    "validTo": "2027-03-05T00:30+00:00"
}
```

## WebSocket

WebSocket端点 /websocket

订阅 /user/queue/alert 接收设备警报

订阅 /user/queue/is-locked 接收门锁状态

## ESP32 -> Broker 发送数据

192.109.228.41:1883

username: alice

password: alice123456

### 更新门锁状态

device/lock

```json
{
  "deviceId": "10001",
  "isLocked": true
}
```

### 确认收到密码

device/lock/code

```json
{
  "deviceId": "10001",
  "codeId": "收到的codeId",
  "code": "收到的code"
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
  "deviceId": 10001,
  "validFrom": "2025-03-22T00:00+08:00",
  "validTo": "2026-02-22T00:00+08:00"
}
```

