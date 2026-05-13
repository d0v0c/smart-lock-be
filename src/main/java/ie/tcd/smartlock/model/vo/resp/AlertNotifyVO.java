package ie.tcd.smartlock.model.vo.resp;

import ie.tcd.smartlock.model.entity.Alert;
import lombok.Data;

/**
 * @author xylingying
 * @date 2025-03-06 22:16
 * @description: 向前端发送警报
 */
@Data
public class AlertNotifyVO {
    private Long deviceId;
    private Alert.Type type;
}
