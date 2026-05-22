package ie.tcd.smartlock.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 警报记录表
 *
 * @TableName alert
 */
@TableName(value = "alert")
@Data
public class Alert {
    /**
     * 警报ID
     */
    @TableId(type = IdType.AUTO)
    private Long alertId;

    /**
     * 通知的用户
     */
    private String username;

    /**
     * 报警的设备
     */
    private Long deviceId;

    /**
     * 警报类型
     */
    private Type type;

    /**
     * 警报内容
     */
    private String message;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    public enum Type {
        MOTOR,
        FINGERPRINT,
        SCREEN,
        LIGHT,
        UNKNOWN
    }
}