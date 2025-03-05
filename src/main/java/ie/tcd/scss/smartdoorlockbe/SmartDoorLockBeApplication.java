package ie.tcd.scss.smartdoorlockbe;

import ie.tcd.scss.smartdoorlockbe.domain.MqttConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(value = MqttConfigurationProperties.class)
@SpringBootApplication
public class SmartDoorLockBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartDoorLockBeApplication.class, args);
    }

}
