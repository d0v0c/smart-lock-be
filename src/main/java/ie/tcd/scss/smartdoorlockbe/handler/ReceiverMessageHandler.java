package ie.tcd.scss.smartdoorlockbe.handler;

import ie.tcd.scss.smartdoorlockbe.service.AccessCodeService;
import ie.tcd.scss.smartdoorlockbe.service.AlertService;
import ie.tcd.scss.smartdoorlockbe.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Component
public class ReceiverMessageHandler implements MessageHandler {
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private AccessCodeService accessCodeService;
    @Autowired
    private AlertService alertService;

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        // header没搞懂
        MessageHeaders headers = message.getHeaders();
        String topic = (String) headers.get("mqtt_receivedTopic");
        System.out.println("接收到消息：" + message);
        if ("device/lock".equals(topic)) {
            // 更新ESP32的上线状态
            deviceService.updateDeviceStatus(message.getPayload().toString());
        } else if ("device/lock/code".equals(topic)) {
            // 收到ESP32的密码确认收到消息
            accessCodeService.confirmUpdate(message.getPayload().toString());
        } else if ("device/lock/alert".equals(topic)) {
            // 收到ESP32的警报消息
            alertService.notifyAlert(message.getPayload().toString());
        } else if ("device/lock/all-code".equals(topic)) {
            accessCodeService.getAllAccessCode(message.getPayload().toString());
        }
    }
}