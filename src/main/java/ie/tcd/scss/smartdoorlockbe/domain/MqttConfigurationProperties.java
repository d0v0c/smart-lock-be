package ie.tcd.scss.smartdoorlockbe.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "spring.mqtt")
public class MqttConfigurationProperties {
    private String username;
    private String password;
    private String url;
    private String subClientId;
    private String subTopic;
    private String pubClientId;
    private Resource certFile;    // fullchain.pem 文件路径
}
