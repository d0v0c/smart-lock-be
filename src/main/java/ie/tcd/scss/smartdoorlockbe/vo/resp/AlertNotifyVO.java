package ie.tcd.scss.smartdoorlockbe.vo.resp;

import ie.tcd.scss.smartdoorlockbe.domain.Alert;
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
