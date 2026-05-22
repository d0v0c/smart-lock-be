package ie.tcd.smartlock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

// record 不能 @Component，所以开启全局扫描
@ConfigurationPropertiesScan
@SpringBootApplication
public class SmartLockBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartLockBeApplication.class, args);
    }

}
