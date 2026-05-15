package ie.tcd.smartlock.mqtt;

import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.mqtt.inbound.Mqttv5PahoMessageDrivenChannelAdapter;
import org.springframework.integration.support.converter.ConfigurableCompositeMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.SmartMessageConverter;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringJUnitConfig
public class MqttBugReproducerTest {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testPayloadConversion() {
        // Construct a raw MQTT v5 message
        String originalJson = "{\"temperature\":25}";
        MqttMessage mqttMessage = new MqttMessage(originalJson.getBytes(StandardCharsets.UTF_8));

        MqttProperties properties = new MqttProperties();
        properties.setContentType("application/json");
        properties.setPayloadFormat(true); // PayloadFormatIndicator=1
        mqttMessage.setProperties(properties);

        //  Initialize the Adapter (bypass actual broker connection)
        Mqttv5PahoMessageDrivenChannelAdapter adapter =
                new Mqttv5PahoMessageDrivenChannelAdapter(
                        "tcp://localhost:1883",
                        "testClient",
                        "test/topic");

        // Trigger the bug
        adapter.setPayloadType(String.class);

        QueueChannel outputChannel = new QueueChannel();
        adapter.setOutputChannel(outputChannel);
        adapter.setBeanFactory(applicationContext);
        adapter.afterPropertiesSet();

        // Trigger messageArrived to simulate message inbound
        adapter.messageArrived("test/topic", mqttMessage);

        // Retrieve result and assert on the expected post-fix behavior.
        // These assertions are written as the framework *should* behave once the bug is fixed:
        //   setPayloadType(String.class) on a v5 message with Content-Type=application/json
        //   and PayloadFormatIndicator=true MUST surface the payload as a String, not byte[].
        // On the current (buggy) Spring Integration version these assertions fail, proving
        // the regression. After the fix they should turn green automatically.
        Message<?> resultMessage = outputChannel.receive(1000);
        assertNotNull(resultMessage, "No message received on outputChannel");

        Object payload = resultMessage.getPayload();
        assertInstanceOf(String.class, payload,
                "Expected payload converted to String per setPayloadType(String.class), " +
                        "but got " + payload.getClass().getName() +
                        (payload instanceof byte[] b ? " = " + new String(b, StandardCharsets.UTF_8) : ""));
        assertEquals(originalJson, payload, "Payload content mismatch");
    }

    @Configuration
    static class TestConfig {
        /**
         * Register the exact converter Spring Integration uses by default:
         * {@link ConfigurableCompositeMessageConverter}.
         * [JacksonJsonMessageConverter(strict), ByteArrayMessageConverter, ObjectStringMessageConverter]
         * and {@code afterPropertiesSet()} appends {@code GenericMessageConverter} once
         * the BeanFactory is wired in.
         */
        @Bean(name = "integrationArgumentResolverMessageConverter")
        public SmartMessageConverter integrationArgumentResolverMessageConverter() {
            return new ConfigurableCompositeMessageConverter();
        }
    }
}

