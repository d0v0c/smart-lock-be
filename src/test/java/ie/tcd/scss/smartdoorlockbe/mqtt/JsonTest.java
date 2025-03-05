package ie.tcd.scss.smartdoorlockbe.mqtt;

import com.alibaba.fastjson2.JSON;
import ie.tcd.scss.smartdoorlockbe.domain.Device;
import org.junit.jupiter.api.Test;

public class JsonTest {
    @Test
    public void jsonTest() {
        // 定义一个 JSON 字符串，包含多余和缺失的字段
        String json = "{"
                + "\"deviceId\": 123,"
                + "\"deviceName\": \"Test Device\","
                + "\"isLocked\": 1,"
                + "\"extraField\": \"This field is not in the class\""
                + "}";

        // 使用 FastJSON 将 JSON 字符串解析为 Device 对象
        Device device = JSON.parseObject(json, Device.class);

        // 打印解析结果
        System.out.println("JSON ---> Device");
        System.out.println("deviceId: " + device.getDeviceId());
        System.out.println("deviceName: " + device.getDeviceName());
        System.out.println("isLocked: " + device.getIsLocked());
        System.out.println("isConnected: " + device.getIsConnected()); // 未提供，应该为 null
        System.out.println("createdTime: " + device.getCreatedTime()); // 未提供，应该为 null
        System.out.println("updatedTime: " + device.getUpdatedTime()); // 未提供，应该为 null
        System.out.println("Device ---> JSON");
        System.out.println(JSON.toJSONString(device));
    }
}
