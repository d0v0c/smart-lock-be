package ie.tcd.scss.smartdoorlockbe.mqtt;

import ie.tcd.scss.smartdoorlockbe.domain.MqttConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MqttConfigurationPropertiesTest {
    @Autowired
    private MqttConfigurationProperties mqttConfigurationProperties;
    @Test
    public void MqttConfigTest() {
        String url = mqttConfigurationProperties.getUrl();
        System.out.println(url);
    }
}
