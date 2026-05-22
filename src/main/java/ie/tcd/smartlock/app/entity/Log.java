package ie.tcd.smartlock.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 日志
 *
 * @TableName log
 */
@TableName(value = "log")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Log {
    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long logId;

    /**
     * 关联设备ID
     */
    private Long deviceId;

    /**
     * 关联用户ID
     */
    private String userId;

    /**
     * 操作类型
     */
    private ActionType actionType;

    /**
     * 操作描述内容
     */
    private String actionDescription;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    public enum ActionType {
        NEW_DEVICE,
        UPDATE_DEVICE,
        CODE_CONFIRMATION,
        ALERT_NOTIFY,
        GET_CODE,
        MQTT_BUSINESS_ERROR,
        MQTT_SYSTEM_ERROR
    }

    public Log(ActionType actionType, String actionDescription) {
        this.actionType = actionType;
        this.actionDescription = actionDescription;
    }
}