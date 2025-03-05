package ie.tcd.scss.smartdoorlockbe.config;

import ie.tcd.scss.smartdoorlockbe.domain.MqttConfigurationProperties;
import ie.tcd.scss.smartdoorlockbe.handler.ReceiverMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttInboundConfiguration {
    @Autowired
    private MqttConfigurationProperties mqttConfigurationProperties;
    @Autowired
    private ReceiverMessageHandler receiverMessageHandler;

    // 配置消息传输通道
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    // 配置入站适配器，设置SubTopic URL ClientId
    @Bean
    public MessageProducer messageProducer(MqttPahoClientFactory mqttPahoClientFactory) {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                mqttConfigurationProperties.getUrl(),
                mqttConfigurationProperties.getSubClientId(),
                mqttPahoClientFactory,
                mqttConfigurationProperties.getSubTopic().split(","));
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    // 配置入站消息处理器
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler messageHandler() {
        return this.receiverMessageHandler;
    }
}
