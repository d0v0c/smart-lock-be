//package ie.tcd.scss.smartdoorlockbe.mqtt;
//
//import org.eclipse.paho.client.mqttv3.*;
//import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
//import org.junit.jupiter.api.Test;
//
//
//public class MqttPahoTest {
//
//    @Test
//    public void sendMqttMsg() throws MqttException {
//        String serverURI = "tcp://52.212.58.78:1883";
//        String clientID = "paho_client";
//        // 1 创建客户端
//        MqttClient mqttClient = new MqttClient(serverURI, clientID, new MemoryPersistence());
//
//        MqttConnectOptions connOpts = new MqttConnectOptions();
//        connOpts.setCleanSession(true);
//        connOpts.setUserName("alice");
//        connOpts.setPassword("123456".toCharArray());
//        // 2 创建连接（输入mqtt用户名+密码）
//        mqttClient.connect(connOpts);
//
//        MqttMessage mqttMessage = new MqttMessage();
//        mqttMessage.setQos(1);
//        mqttMessage.setPayload("hello from paho".getBytes());
//        // 3 发送消息（指定topic和payload）
//        mqttClient.publish("lock/code", mqttMessage);
//
//        // 4 断开连接
//        mqttClient.disconnect();
//        mqttClient.close();
//    }
//
//    @Test
//    public void receiveMqttMsg() throws MqttException {
//        String serverURI = "tcp://52.212.58.78:1883";
//        String clientID = "paho_client";
//        MqttClient mqttClient = new MqttClient(serverURI, clientID, new MemoryPersistence());
//
//        MqttConnectOptions connOpts = new MqttConnectOptions();
//        connOpts.setCleanSession(true);
//        connOpts.setUserName("alice");
//        connOpts.setPassword("123456".toCharArray());
//
//        mqttClient.setCallback(new MqttCallback() {
//            @Override
//            public void connectionLost(Throwable throwable) {
//                System.out.println("Connection lost...");
//            }
//
//            @Override
//            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
//                System.out.println("Topic: " + topic);
//                System.out.println("Message arrived: " + new String(mqttMessage.getPayload()));
//            }
//
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
//                System.out.println("Delivery complete");
//            }
//        });
//        mqttClient.connect(connOpts);
//        mqttClient.subscribe("lock/code", 1);
//
/// /        while (true) ;
//    }
//}
