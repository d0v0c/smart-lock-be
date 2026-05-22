package ie.tcd.smartlock.mqtt;

import org.springframework.http.MediaType;
import org.springframework.integration.annotation.GatewayHeader;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;

@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel",
        defaultHeaders = @GatewayHeader(
                name = MessageHeaders.CONTENT_TYPE, value = MediaType.APPLICATION_JSON_VALUE))
public interface MqttGateway {
    /**
     * 发送 MQTT 消息
     *
     * @param topic   主题
     * @param payload 内容
     */
    void send(@Header(MqttHeaders.TOPIC) String topic, Object payload);

    /**
     * 发送 MQTT v5 新加的 RPC 的消息
     */
    void sendRpc(@Header(MqttHeaders.TOPIC) String topic,
                 @Header(MqttHeaders.RESPONSE_TOPIC) String responseTopic,
                 @Header(MqttHeaders.CORRELATION_DATA) byte[] correlationData,
                 Object payload);
}
