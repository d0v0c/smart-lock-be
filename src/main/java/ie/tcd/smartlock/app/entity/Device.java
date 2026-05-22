package ie.tcd.smartlock.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

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
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}