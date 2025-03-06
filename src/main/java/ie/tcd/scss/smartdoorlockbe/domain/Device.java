package ie.tcd.scss.smartdoorlockbe.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.ZonedDateTime;

/**
 * ESP32设备表
 *
 * @TableName device
 */
@TableName(value = "device")
@Data
public class Device {
    @TableId(type = IdType.AUTO)
    private Long deviceId;
    private String deviceName;
    private Boolean isLocked;
    private Boolean isConnected;
    private ZonedDateTime createdTime;
    private ZonedDateTime updatedTime;
}