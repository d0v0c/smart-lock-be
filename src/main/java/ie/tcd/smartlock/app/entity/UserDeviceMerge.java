package ie.tcd.smartlock.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户与门禁的多对多关系
 *
 * @TableName user_device_merge
 */
@TableName(value = "user_device_merge")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDeviceMerge {
    /**
     * 用户名
     */
    private String username;

    /**
     * 设备号
     */
    private Long deviceId;
}