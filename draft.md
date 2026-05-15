# Smart Door Lock Backend

> 基于 **Spring Boot 4 / Java 25** 的物联网智能门锁后端服务，桥接移动端 App 与 ESP32 设备，提供远程控制与实时监测功能。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)](https://openjdk.org/projects/jdk/25/)
[![MQTT](https://img.shields.io/badge/MQTT-v5-660066?logo=mqtt)](https://mqtt.org/)

---

## 项目概述

项目需要在三方间通信：**移动端 App ↔ 后端 ↔ ESP32 设备**。

- App 用 HTTP / WebSocket（服务端推送），适合普通网络环境。
- ESP32 用 MQTT（长连接 + 低功耗），适合资源受限的嵌入式设备。

本后端服务作为协议桥梁，承担鉴权、命令下发、状态同步、告警推送、密码生命周期管理等职责。

## 系统架构

整体架构：

![architecture](./.assets/architecture.png)

后端内部模块：

![backend](./.assets/backend.png)

## 技术栈

| 维度  | 选型                                  | 说明                        |
|-----|-------------------------------------|---------------------------|
| 框架  | Spring Boot 4.0 / Java 25           | 启用虚拟线程（协程）                |
| 持久层 | MySQL / MyBatis-Plus                | Lambda + Base 类 快速实现 CRUD |
| 缓存  | Redis / Spring Cache                | 查询缓存、JWT 鉴权、防抖锁           |
| 鉴权  | Spring Security / OAuth2            | Ed25519 签名、双 JWT 机制       |
| 物联网 | Spring Integration / MQTT v5 / EMQX | 线程分片入站                    |
| 推送  | WebSocket / STOMP                   | 门锁状态、实时告警                 |
| 部署  | Docker Compose + Nginx + TLS        | 一键打包启动                    |

---

## 项目亮点

### 1. HTTP ↔ MQTT 异步转同步

> 利用 虚拟线程 + CompletableFuture 实现

App 调用 `POST /api/code` 生成临时密码时，业务流程是：

```
App ──HTTP──▶ Backend ──MQTT──▶ ESP32 ──MQTT ack──▶ Backend ──HTTP 200──▶ App
                  │                                       ▲
                  └─── 在此挂起，等待 ack（最多 30s）───────┘
```

后端用 `ConcurrentHashMap<Long, CompletableFuture<String>>` 存储待确认的消息，借助 Java 25 虚拟线程，实现 `future.get()`
写法阻塞、实际后台非阻塞挂起：

```java
private final ConcurrentHashMap<Long, CompletableFuture<String>> pendingAccessCodeMap = new ConcurrentHashMap<>();

public String generateCode(...) {
    CompletableFuture<String> future = new CompletableFuture<>();
    pendingAccessCodeMap.put(codeId, future);
    mqttGateway.send("server/" + deviceId + "/code", json);
    return future.get(30, TimeUnit.SECONDS);   // 虚拟线程被卸载
}
```

收到 MQTT 确认后由分片线程 `future.complete()`，唤醒等待中的虚拟线程继续执行落库与响应。

> 详见 `service/AccessCodeService.java`

---

### 2. 多线程下，单设备消息保序

> 利用 分片线程池，基于 DeviceID 哈希分发 实现

问题定位：MQTT 入站消息经 Spring Integration 流式处理。如果直接交给同一个线程池并行消费，一台设备的 "开锁、上锁" 状态落库时间可能
**乱序**，导致库中状态与真实状态不符。（例：消息按照 `开锁 → 上锁` 顺序到达，但是被 `上锁 → 开锁` 顺序落库，则库中最终状态为开锁）

解决思路：将消息按 `deviceId` 哈希到 16 个 **strand**（每个 strand = 单线程 + 阻塞队列）。每个 `deviceId` 永远被分配到一个固定的线程，保证
**同一设备**的消息严格按**到达顺序串行执行**。跨设备之间保持并行。

```
入站线程 ─▶ 提取 deviceId ─▶ strand = deviceId.hash() mod 16
                                       │
                          ┌────────────┼────────────┐
                          ▼            ▼            ▼
                      strand-0     strand-1 ... strand-15
                       (queue)      (queue)      (queue)
                       (thread)     (thread)     (thread)
                          └────────────┼────────────┘
                                       ▼
                             afterStrandFlow（线程分发后，继续执行后续流程）
```

队列满时，通过 `RejectedExecutionHandler` 阻塞 Paho 入站线程，防止 OOM。

> 详见 `mqtt/MqttStrandConfig.java`、`mqtt/MqttFlowConfig.java`

---

### 3. MQTT v5 Header 隐式反序列化

> 利用框架的 MessageConverter 默认转换函数，省去 Jackson 手动转换，精简处理链路

MQTT v5 引入了 `Content-Type` 与 `Payload Format Indicator` 元数据。
ESP32 发布消息时携带消息头：

```
Content-Type: application/json
Payload Format Indicator: true
```

Spring Integration 即可根据 `Header` 里的 `Content-Type` + `TargetClass.class`，调用框架内置的转换链，直接拿到强类型
POJO，省去手动 `objectMapper.readValue(...)`。

---

### 4. 开源贡献：Spring Integration MQTT v5 Adapter 反序列化 Bug

配置数据处理链路时，发现 `Mqttv5PahoMessageDrivenChannelAdapter#setPayloadType(String.class)` 这个配置实际不生效。框架内部仍将
payload 作为 `byte[]` 透传。

通过阅读源码定位到 `Mqttv5PahoMessageDrivenChannelAdapter` 反序列化函数调用错误问题，是 `MQTT v3 → v5` 迁移过程中遗留的
bug。后续向 [spring-integration](https://github.com/spring-projects/spring-integration) 仓库提交了包含 bug 复现 +
根因分析 的 [Issue](https://github.com/spring-projects/spring-integration/issues/10990)。

---

### 5. 长短双 JWT 无状态鉴权

| Token         | 有效期    | 用途          |
|---------------|--------|-------------|
| Access Token  | 短（分钟级） | 业务 API 鉴权   |
| Refresh Token | 长（天级）  | 仅用于换新 Token |

- 签名算法：`Ed25519`（EdDSA）。绕开 Nimbus 对 Ed25519 的不完整支持（依赖第三方密码库），直接调用 JDK 自带的
  `java.security.Signature("Ed25519")` 实现原生轻量级 `JwtEncoder` / `JwtDecoder`。
- 独立校验：通过两个独立 `JwtDecoder`，根据 claim typ，分别校验 Access Token、Refresh Token，防止互相冒用。
- Rotation：每次 refresh 都会签发一对新 token，旧 Refresh Token 在 Redis 中被覆盖，立即失效。
- 无状态：服务端不持有 Session ID。

> 详见 `security/SecurityConfig.java`

---

### 6. 防重复点击

> 利用 AOP + Redis SETNX 防抖锁 实现

问题定位：App 网络抖动可能导致用户重复提交 生成密码、修改信息 等 POST/PUT 请求。

解决思路：在 Controller 切面上拦截非幂等请求（POST/PUT/DELETE），按 `user + method + URI [+ deviceId]` 在 Redis 上 `SETNX` 加
TTL 锁，缓存未过期视为重复请求，直接返回 `429 Too Many Requests`。

```java

@Debounce(timeout = 31_000, releaseOnSuccess = true, key = "#request.deviceId")
@PostMapping("/api/code")
public Result<String> generateCode(@RequestBody AccessCodeGenerateReqVO request, ...) { ...}
```

- 用户身份解析优先级：`Authentication` → `X-Forwarded-For` → `RemoteAddr`，兼顾登录与匿名场景。
- Redis 故障时降级放行，不影响主流程。
- 通过 `@Debounce` 注解 + SpEL 表达式自定义 key（如以 `deviceId` 为 key）。

> 详见 `aspect/DebounceAspect.java`

---

## Quick Start

安装 Docker Compose。

```bash
docker compose up -d --build
```

服务启动后访问：

- Swagger UI：<http://localhost:8080/swagger-ui/index.html>
- EMQX Dashboard：<http://localhost:18083>
- MQTT Broker：`tcp://localhost:1883`（默认）/ `ssl://localhost:8883`（需配置证书）

---

## API 文档

完整在线文档见 Swagger UI。以下为核心接口示例。

### 注册 / 登录 / 更新

```http
POST /api/user/register
{ "username": "alfa", "password": "123456", "email": "a@b.cd" }

POST /api/user/login
{ "username": "alfa", "password": "123456" }
→ { "accessToken": "...", "refreshToken": "..." }

POST /api/user/refresh
{ "refreshToken": "<refresh JWT>" }
→ { "accessToken": "...", "refreshToken": "..." }
```

### 业务接口

```http
GET  /api/device                      # 查询当前用户绑定的设备
Authorization: Bearer <access JWT>

POST /api/code                        # 生成临时密码
{ "deviceId": 10001, "validFrom": "...", "validTo": "..." }
```

### WebSocket（STOMP）

- 端点：`/websocket`
- `/user/queue/alert`：接收设备告警
- `/user/queue/is-locked`：接收门锁状态变更

---

## MQTT 主题约定

> Broker：`tcp://localhost:1883`

### 后端 → 设备

| Topic                  | QoS | 用途         |
|------------------------|-----|------------|
| `server/{id}/code`     | 1   | 下发新生成的临时密码 |
| `server/{id}/all-code` | 1   | 提供设备所有密码   |

### 设备 → 后端

| Topic                  | QoS | 用途                                           |
|------------------------|-----|----------------------------------------------|
| `device/{id}/status`   | 0   | 上报锁定状态                                       |
| `device/{id}/code`     | 1   | 确认收到密码                                       |
| `device/{id}/alert`    | 1   | 上报硬件告警（MOTOR / FINGERPRINT / SCREEN / LIGHT） |
| `device/{id}/all-code` | 1   | 请求拉取所有密码                                     |