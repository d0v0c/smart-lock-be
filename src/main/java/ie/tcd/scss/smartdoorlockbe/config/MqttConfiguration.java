package ie.tcd.scss.smartdoorlockbe.config;

import ie.tcd.scss.smartdoorlockbe.domain.MqttConfigurationProperties;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;

@Configuration
public class MqttConfiguration {
    @Autowired
    private MqttConfigurationProperties mqttConfigurationProperties ;

    @Bean
    public MqttPahoClientFactory mqttPahoClientFactory() {
        // 创建客户端工厂
        DefaultMqttPahoClientFactory mqttPahoClientFactory = new DefaultMqttPahoClientFactory();

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(mqttConfigurationProperties.getUsername());
        options.setPassword(mqttConfigurationProperties.getPassword().toCharArray());
        options.setServerURIs(new String[]{mqttConfigurationProperties.getUrl()});
        // 设置 mqtt用户名+密码+URL
        mqttPahoClientFactory.setConnectionOptions(options);

        // 返回客户端工厂
        return mqttPahoClientFactory;
    }
}
