package ie.tcd.smartlock.mqtt;

import ie.tcd.smartlock.config.SmartLockProperties;
import ie.tcd.smartlock.model.entity.Alert;
import ie.tcd.smartlock.model.entity.Device;
import ie.tcd.smartlock.model.entity.Log;
import ie.tcd.smartlock.model.vo.resp.AccessCodeRespMqtt;
import ie.tcd.smartlock.service.AccessCodeService;
import ie.tcd.smartlock.service.AlertService;
import ie.tcd.smartlock.service.DeviceService;
import ie.tcd.smartlock.service.LogService;
import ie.tcd.smartlock.utils.BusinessException;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.mqtt.core.Mqttv5ClientManager;
import org.springframework.integration.mqtt.inbound.Mqttv5PahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.Mqttv5PahoMessageHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

import javax.net.ssl.SSLSocketFactory;
import java.nio.charset.StandardCharsets;

/**
 * 必须写配置类，不能 AutoConfiguration，
 * 因为没有 starter-mqtt
 */
@Configuration
public class MqttFlowConfig {
    /**
     * MQTT v5 的连接配置。连接 Broker，一条 TCP 链路，单例复用
     */
    @Bean
    public Mqttv5ClientManager mqttv5ClientManager(SmartLockProperties properties) {
        SmartLockProperties.Mqtt mqttProps = properties.mqtt();
        MqttConnectionOptions options = new MqttConnectionOptions();
        String url = mqttProps.url();

        options.setServerURIs(new String[]{url});
        options.setUserName(mqttProps.username());
        options.setPassword(mqttProps.password().getBytes(StandardCharsets.UTF_8));
        // 开启自动重连与离线消息清理策略
        options.setAutomaticReconnect(true);
        options.setCleanStart(false);
        options.setSessionExpiryInterval(3600L);
        // 根据 URL 前缀动态调整是否加密
        if (url != null && url.startsWith("ssl://")) {
            options.setSocketFactory(SSLSocketFactory.getDefault());
        }

        String clientId = properties.mqtt().clientId();
        return new Mqttv5ClientManager(options, clientId);
    }

    /**
     * 发送 MQTT 消息
     * MessageHandler 出站通道适配器
     */
    @Bean
    public IntegrationFlow mqttOutboundFlow(Mqttv5ClientManager clientManager) {
        Mqttv5PahoMessageHandler messageHandler = new Mqttv5PahoMessageHandler(clientManager);
        // 设置默认的发布 Topic 和 QoS（可以在 Message Headers 中动态覆盖）
        messageHandler.setDefaultTopic("server");
        messageHandler.setDefaultQos(1);    // 发布的默认 QoS
        messageHandler.setAsync(true);

        // 隐式 Lambda
//        return f -> f.handle(messageHandler);
        return IntegrationFlow
                .from("mqttOutboundChannel")
                .handle(messageHandler)
                .get();
    }

    /**
     * 接收消息
     * Adapter 入站通道适配器
     */
    @Bean
    public IntegrationFlow mqttInboundFlow(Mqttv5ClientManager clientManager,
                                           DeviceService deviceService,
                                           AccessCodeService accessCodeService,
                                           AlertService alertService) {
        // 配置入站
        Mqttv5PahoMessageDrivenChannelAdapter adapter = new Mqttv5PahoMessageDrivenChannelAdapter(clientManager, "device");

        // 相当于 v3 里的 DefaultPahoMessageConverter，把 byte[] payload 转换成 String，只不过框架有bug，代码其实是无效的。
//        adapter.setPayloadType(String.class);
        adapter.addTopic("device/+/status", 0);
        adapter.addTopic("device/+/code", 1);
        adapter.addTopic("device/+/alert", 1);
        adapter.addTopic("device/+/all-code", 1);
        adapter.setErrorChannelName("errorChannel");

        return IntegrationFlow
                .from(adapter)  // 生成了一个匿名的、隐形的 mqttInputChannel
//                .transform(Transformers.objectToString())   // 给 setPayloadType 擦屁股
                .log(LoggingHandler.Level.WARN, "MQTT-String-Log", m -> "转换后的字符串: " + m.getPayload() + "\n转换后的Header：" + m.getHeaders())
                // 预处理：拦截消息，解析 Topic，把结果打上标签存入 Headers
                .enrichHeaders(h -> h
                        // 提取 deviceId 存入自定义的 "extracted_deviceId"
                        .headerFunction("extracted_deviceId", m -> {
                            String topic = m.getHeaders().get(MqttHeaders.RECEIVED_TOPIC, String.class);
                            String[] parts = topic.split("/");
                            return parts.length >= 2 ? Long.parseLong(parts[1]) : -1L;
                        })
                        // 提取 action 存入自定义的 "extracted_action"
                        .headerFunction("extracted_action", m -> {
                            String topic = m.getHeaders().get(MqttHeaders.RECEIVED_TOPIC, String.class);
                            String[] parts = topic.split("/");
                            return parts.length >= 3 ? parts[2] : "unknown";
                        })
                )
                // 提取路由键 (extracted_action) 分流
                .route(Message.class, m -> m.getHeaders().get("extracted_action", String.class),
                        // 配置路由映射关系
                        mapping -> mapping
                                .subFlowMapping("status", sf -> sf.handle(Device.class,
                                        (payload, headers) -> {
                                            Thread t = Thread.currentThread();
                                            System.out.println("isVirtual=" + t.isVirtual() + ", name=" + t.getName());
                                            Long deviceId = headers.get("extracted_deviceId", Long.class);
                                            deviceService.updateDeviceStatus(payload, deviceId);
                                            return null;
                                        }))
                                .subFlowMapping("code", sf -> sf.handle(AccessCodeRespMqtt.class,
                                        (payload, headers) -> {
                                            Long deviceId = headers.get("extracted_deviceId", Long.class);
                                            accessCodeService.confirmUpdate(payload, deviceId);
                                            return null;
                                        }))
                                .subFlowMapping("alert", sf -> sf.handle(Alert.class,
                                        (payload, headers) -> {
                                            Long deviceId = headers.get("extracted_deviceId", Long.class);
                                            alertService.notifyAlert(payload, deviceId);
                                            return null;
                                        }))
                                .subFlowMapping("all-code", sf -> sf.handle(AccessCodeRespMqtt.class,
                                        (payload, headers) -> {
                                            Long deviceId = headers.get("extracted_deviceId", Long.class);
                                            accessCodeService.getAllAccessCode(payload, deviceId);
                                            return null;
                                        }))
                                // 如果收到不在上述列表中的 Topic
                                .resolutionRequired(false) // 允许匹配不到路由
                                .defaultSubFlowMapping(sf -> sf
                                        .log(LoggingHandler.Level.WARN, m -> "收到未知的 Topic，消息被丢弃：" + m.getHeaders().get(MqttHeaders.RECEIVED_TOPIC))
                                        .nullChannel()
                                )
                )
                .get();
    }

    /**
     * 全局异常处理 flow：按异常类型分发，统一落库到 log 表。
     * 复用 Spring Integration 默认的 errorChannel；inbound flow 抛出的异常会自动到这里。
     */
    // ErrorMessage -> MessageHandlingException -> cause / failed
    //{                                                  // ❶ ErrorMessage 信封，Throwable.class
    //  "headers": { ... },                              // ← 函数参数 headers ★
    //  "payload": {                                     // ← 函数参数 t ★
    //    "_type": "MessageHandlingException / ...",     // (t 的运行时类型)
    //    "cause": {                                     // ← 局部变量 cause ★
    //      "_type": "BusinessException / NPE / ...",    // (cause 的运行时类型，决定走哪个 if 分支)
    //      "message": "...",                            // (be.getMessage() / cause.getMessage())
    //      "cause": null
    //    },
    //    "failedMessage": {                             // ← 局部变量 failed ★
    //      "_type": "GenericMessage<?>",
    //      "payload": "byte[]",
    //      "headers": {
    //        "mqtt_receivedTopic": "device/10001/status",// ← 局部变量 topic ★
    //        "mqtt_receivedQos": 0,
    //        "extracted_deviceId": 10001,               // ← 局部变量 deviceId ★ (经 parseDeviceId 转 Long)
    //        "extracted_action": "status",
    //        "contentType": "application/json",
    //      }
    //    }
    //  }
    //}
    @Bean
    public IntegrationFlow mqttErrorFlow(LogService logService) {
        return IntegrationFlow.from("errorChannel")
                .handle(Throwable.class, (t, headers) -> {
                    Throwable cause = t.getCause() != null ? t.getCause() : t;
                    Message<?> failed = (t instanceof MessagingException me) ? me.getFailedMessage() : null;
                    String topic = failed != null
                            ? failed.getHeaders().get(MqttHeaders.RECEIVED_TOPIC, String.class)
                            : null;
                    Long deviceId = parseDeviceId(failed);

                    Log log;
                    if (cause instanceof BusinessException be) {
                        log = new Log(Log.ActionType.MQTT_BUSINESS_ERROR,
                                String.format("topic=%s, code=%s, message=%s",
                                        topic, be.getStatusCode(), be.getMessage()));
                    } else {
                        log = new Log(Log.ActionType.MQTT_SYSTEM_ERROR,
                                String.format("topic=%s, error=%s, message=%s",
                                        topic, cause.getClass().getSimpleName(), cause.getMessage()));
                    }
                    log.setDeviceId(deviceId);
                    logService.save(log);
                    return null;
                })
                .get();
    }

    private static Long parseDeviceId(Message<?> failed) {
        if (failed == null) return null;
        Object id = failed.getHeaders().get("extracted_deviceId");
        if (id == null) return null;
        try {
            return Long.valueOf(id.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
