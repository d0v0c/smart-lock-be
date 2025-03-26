package ie.tcd.scss.smartdoorlockbe.config;

import ie.tcd.scss.smartdoorlockbe.domain.MqttConfigurationProperties;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;

@Configuration
public class MqttConfiguration {
    @Autowired
    private MqttConfigurationProperties mqttConfigurationProperties;

    @Bean
    public MqttPahoClientFactory mqttPahoClientFactory() {
        // 创建客户端工厂
        DefaultMqttPahoClientFactory mqttPahoClientFactory = new DefaultMqttPahoClientFactory();

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(mqttConfigurationProperties.getUsername());
        options.setPassword(mqttConfigurationProperties.getPassword().toCharArray());
        options.setServerURIs(new String[]{mqttConfigurationProperties.getUrl()});
//        options.setCleanSession(true);


        try {
            // TLS 配置
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//            KeyStore keyStore = KeyStore.getInstance("PKCS12");
//            keyStore.load(null);
//            keyStore.setCertificateEntry("server-cert", cert);
            // 加载服务器证书
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream certStream = mqttConfigurationProperties.getCertFile().getInputStream();
            Collection<? extends Certificate> certs = cf.generateCertificates(certStream);

            // 创建KeyStore并导入证书
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            int i = 0;
            for (Certificate cert : certs) {
                ks.setCertificateEntry("cert-" + i++, cert);
            }
            // 创建TrustManagerFactory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            // 创建SSLContext
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, tmf.getTrustManagers(), new SecureRandom());
            options.setSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 加载服务器私钥
//            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(readPrivateKey(properties.getPrivateKeyPath()));
//            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
//            keyStore.setKeyEntry("server-key", privateKey, new char[0], new Certificate[]{cert});
//
//            keyManagerFactory.init(keyStore, new char[0]);
//            sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
//
//            options.setSocketFactory(sslContext.getSocketFactory());


        // 设置 mqtt用户名+密码+URL
        mqttPahoClientFactory.setConnectionOptions(options);


        // 返回客户端工厂
        return mqttPahoClientFactory;
    }
}
