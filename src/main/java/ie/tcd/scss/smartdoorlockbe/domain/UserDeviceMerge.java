package ie.tcd.scss.smartdoorlockbe.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户与门禁的多对多关系
 * @TableName user_device_merge
 */
@TableName(value ="user_device_merge")
@Data
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