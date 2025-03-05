package ie.tcd.scss.smartdoorlockbe.service;

import ie.tcd.scss.smartdoorlockbe.gateway.MqttGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MqttMessageSender {
    @Autowired
    private MqttGateway mqttGateway;

    /**
     * 发送mqtt消息
     * @param topic 主题
     * @param message 内容
     */
    public void send(String topic, String message) {
        mqttGateway.sendToMqtt(topic, message);
    }

    /**
     * 发送包含qos的消息
     * @param topic 主题
     * @param qos 质量
     * @param message 消息体
     */
    public void send(String topic, int qos, String message){
        mqttGateway.sendToMqtt(topic, qos, message);
    }
}
