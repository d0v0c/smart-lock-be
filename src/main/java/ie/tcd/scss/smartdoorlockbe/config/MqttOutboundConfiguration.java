package ie.tcd.scss.smartdoorlockbe.config;

import ie.tcd.scss.smartdoorlockbe.domain.MqttConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttOutboundConfiguration {
    @Autowired
    private MqttConfigurationProperties mqttConfigurationProperties;
    @Autowired
    private MqttPahoClientFactory pahoClientFactory;

    @Bean
    public MessageChannel mqttOutputChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutputChannel")
    public MessageHandler mqttOutboundMassageHandler() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(
                mqttConfigurationProperties.getUrl(),
                mqttConfigurationProperties.getPubClientId(),
                pahoClientFactory);
        messageHandler.setAsync(true);
        messageHandler.setDefaultQos(0);
        messageHandler.setDefaultTopic("default");
        return messageHandler;
    }
}
