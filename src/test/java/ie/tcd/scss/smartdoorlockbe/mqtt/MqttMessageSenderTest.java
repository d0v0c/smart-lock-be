package ie.tcd.scss.smartdoorlockbe.mqtt;

import ie.tcd.scss.smartdoorlockbe.SmartDoorLockBeApplication;
import ie.tcd.scss.smartdoorlockbe.service.MqttMessageSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SmartDoorLockBeApplication.class)
public class MqttMessageSenderTest {
    @Autowired
    private MqttMessageSender mqttMessageSender;

    @Test
    public void sendToMqtt() {
        mqttMessageSender.send("lock/code", "msg from sendToMqtt()");

    }
}
