# Spring Boot 4 + Java 25 迁移计划

> 项目:smart-door-lock-be
> 起点:Spring Boot 3.4.2 + Java 17
> 目标:Spring Boot 4.x + Java 25 (LTS)
> 策略:OpenRewrite 自动化为主,手工修正为辅,分阶段提交便于回滚

---

## 0. 总览

| 维度 | 现状 | 目标 |
|---|---|---|
| Spring Boot | 3.4.2 | 4.0.x |
| Spring Framework | 6.x | 7.x |
| Spring Security | 6.x | 7.x |
| Spring Integration | 6.x | 7.x |
| Java | 17 | 25 |
| Jackson | 2.x (`com.fasterxml.jackson`) | 3.x (`tools.jackson`) |
| Jakarta EE | 10 | 11 |

迁移分支:`chore/upgrade-springboot4-java25`

---

## 1. 模块 A:构建配置

### 涉及文件
- `pom.xml`
- `.mvn/wrapper/maven-wrapper.properties`

### 改动
1. parent 升到 `spring-boot-starter-parent:4.0.x`
2. `<java.version>` 改为 `25`
3. 移除显式版本固定的依赖,交由 BOM 管理:
   - `jackson-datatype-jsr310`(已被 BOM 管理)
4. 替换 starter:
   - `mybatis-plus-spring-boot3-starter` → `mybatis-plus-spring-boot4-starter`
5. 升级:
   - `springdoc-openapi-starter-webmvc-ui` 2.8.5 → 3.x
   - `lombok` 显式锁定 ≥1.18.36(Java 25 支持)
   - `fastjson2`、`hutool-all`、`modelmapper` 升到最新
6. Maven Wrapper:3.9.9 已可,可选升 3.9.10+

### 工具
- OpenRewrite:`org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0`

---

## 2. 模块 B:Java 源码语言级升级

### 影响面
全部 `src/main/java/**` 与 `src/test/java/**`。

### 改动
1. `--release 25` 编译,旧字节码 target 提升
2. 处理 Java 18+ 中 deprecated 的 API(项目目前未直接使用)
3. 反射开放性收紧:Lombok / ModelMapper / fastjson2 必须使用 Java 25 兼容版本
4. JVM 启动参数(运行/测试时):必要时加 `--enable-native-access=ALL-UNNAMED`

### 工具
- OpenRewrite:`org.openrewrite.java.migrate.UpgradeToJava25`
- `jdeps --jdk-internals` 扫描 jar

---

## 3. 模块 C:Jackson 3 迁移(破坏性最大)

### 涉及文件
- `src/main/java/.../config/RedisCacheConfig.java`(直接 import Jackson)
- 所有间接通过 Spring 默认 `ObjectMapper` 工作的位置(无需改代码,只要 Spring 自动配置切换)

### 改动
1. import:`com.fasterxml.jackson.*` → `tools.jackson.*`
2. `JavaTimeModule` 包路径变更
3. `ObjectMapper#activateDefaultTyping` 在 Jackson 3 中 API 变化,需要回归测试 Redis 缓存反序列化
4. `GenericJackson2JsonRedisSerializer` 在 Spring Data Redis 4.x 中可能有新版本

### 工具
- OpenRewrite:`org.openrewrite.java.jackson.UpgradeJackson_3_x`

### 验证
- Redis 缓存对象往返(`AccessCode` 等)
- `ZonedDateTime` 序列化格式

---

## 4. 模块 D:Spring Security 7

### 涉及文件
- `src/main/java/.../config/SecurityConfig.java`

### 改动
1. `BearerTokenAuthenticationEntryPoint` / `BearerTokenAccessDeniedHandler` 包路径在 Security 6.4+ 已变,7.x 需核对
2. `SecurityFilterChain` DSL:`httpBasic`、`csrf`、`sessionManagement`、`oauth2ResourceServer` lambda 形式保留;检查是否有方法被移除
3. `UserDetailsService` 用法不变
4. JWT(NimbusJwtDecoder/Encoder)无变化

### 工具
- OpenRewrite:`org.openrewrite.java.spring.security6.UpgradeSpringSecurity_7`(若存在)
- 否则手工根据编译错误修正

---

## 5. 模块 E:Spring Integration 7 + MQTT

### 涉及文件
- `src/main/java/.../config/MqttConfiguration.java`
- `src/main/java/.../config/MqttOutboundConfiguration.java`
- `src/main/java/.../config/MqttInboundConfiguration.java`
- `src/main/java/.../gateway/MqttGateway.java`
- `src/main/java/.../service/MqttMessageSender.java`
- `src/main/java/.../handler/ReceiverMessageHandler.java`
- `src/main/java/.../domain/MqttConfigurationProperties.java`

### 改动
1. **本期保留 Paho v3 客户端**(SI 7 仍支持),只升级 SI 版本
2. `@MessagingGateway`、`@ServiceActivator`、`DefaultMqttPahoClientFactory`、`MqttPahoMessageHandler`、`MqttPahoMessageDrivenChannelAdapter` API 大体不变
3. 检查 `MessageHandler#handleMessage` 接口 / `Message<?>` headers 常量 key

### 后续(独立 PR,不在本次)
- Paho v3 → Paho v5(`Mqttv5PahoMessageDrivenChannelAdapter` + `MqttPahoMessageHandlerV5`),获得 MQTT 5 特性

---

## 6. 模块 F:WebSocket / STOMP

### 涉及文件
- `src/main/java/.../config/WebSocketConfig.java`
- `src/main/java/.../handler/WebSocketEventListener.java`

### 改动
1. `setAllowedOrigins("*")` 与 `withSockJS()`/credentials 共用时仅 `setAllowedOriginPatterns` 工作 — 当前未启 SockJS,可暂不动
2. `EnableWebSocketMessageBroker`、`WebSocketMessageBrokerConfigurer` API 在 Framework 7 中保留
3. STOMP 事件类(`SessionConnectedEvent` 等)位置不变

---

## 7. 模块 G:数据层(MyBatis-Plus + MySQL + Redis)

### 涉及文件
- `pom.xml`
- `src/main/resources/application.yaml`
- `src/main/resources/mapper/*.xml`
- `src/main/java/.../mapper/*.java`
- `src/main/java/.../config/RedisCacheConfig.java`

### 改动
1. MyBatis-Plus starter 切到 Boot 4 版本
2. `application.yaml` 中 `mybatis-plus.configuration.log-impl` 路径若 MyBatis 升级到新版需核对;XML mapper 不动
3. `LettuceConnectionFactory` 无参构造已 deprecated → 改用 `RedisStandaloneConfiguration`(可在本期一并改)
4. `spring.cache.redis.time-to-live` property key 不变

---

## 8. 模块 H:OpenAPI / Springdoc

### 涉及文件
- `pom.xml`(版本)
- `src/main/java/.../config/OpenApiConfig.java`

### 改动
- Springdoc 3.x 适配 Boot 4 + Jackson 3。`OpenAPI` / `SecurityScheme` API 保持稳定,代码大概率零改动。

---

## 9. 模块 I:测试

### 涉及文件
- `src/test/java/.../SmartDoorLockBeApplicationTests.java`
- `src/test/java/.../mqtt/*Test.java`(部分已注释)

### 改动
- 仅版本随 BOM 升级
- `@SpringBootTest` API 不变
- 注意 Mockito + Java 25:可能需要 Mockito 5.14+(Boot 4 BOM 应已对齐)

---

## 10. 执行顺序

| 阶段 | 动作 | 工具 |
|---|---|---|
| 0 | 切分支、跑现状 build 作基线 | git, mvn |
| 1 | 配置 OpenRewrite 插件(在 pom 加 plugin) | rewrite-maven-plugin |
| 2 | 跑 `UpgradeSpringBoot_4_0` recipe | OpenRewrite |
| 3 | 跑 `UpgradeToJava25` recipe | OpenRewrite |
| 4 | 跑 `UpgradeJackson_3_x` recipe | OpenRewrite |
| 5 | 替换 MyBatis-Plus starter / Springdoc 等 OpenRewrite 改不到的依赖 | 手工 |
| 6 | `mvn -DskipTests clean compile` 修编译错 | 手工 |
| 7 | `mvn test` 修测试 | 手工 |
| 8 | 启动应用,跑 MQTT / JWT / WebSocket / 文件上传下载烟测 | 手工 |
| 9 | 提交、合并 | git |

---

## 11. 风险清单

| 风险 | 模块 | 应对 |
|---|---|---|
| Jackson 3 默认类型化反序列化语义变化 | C/G | 回归测试 Redis 缓存,必要时显式配置 `PolymorphicTypeValidator` |
| Spring Security 7 包路径破坏性变更 | D | 编译失败处手工修正 |
| ModelMapper Java 25 反射兼容 | A/G | 失败则迁 MapStruct |
| Paho v3 在 SI 7 的兼容性 | E | 不行就提前到本期升 v5 |
| OpenRewrite recipe 覆盖不全 | 全 | 编译错误手工补 |

---

## 12. 回滚

每阶段单独 commit,失败可 `git reset --hard <hash>`。最终未通过验证则放弃分支,main 不受影响。