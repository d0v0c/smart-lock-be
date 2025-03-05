package ie.tcd.scss.smartdoorlockbe.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.ZonedDateTime;
import lombok.Data;

/**
 * 门锁密码表
 * @TableName access_code
 */
@TableName(value ="access_code")
@Data
public class AccessCode {
    /**
     * 密码ID
     */
    @TableId(type = IdType.AUTO)
    private Long codeId;

    /**
     * 密码
     */
    private String code;

    /**
     * 关联设备ID
     */
    private Long deviceId;

    /**
     * 所有者ID
     */
    private String owner;

    /**
     * 有效期起始时间
     */
    private ZonedDateTime validFrom;

    /**
     * 有效期结束时间
     */
    private ZonedDateTime validTo;

    /**
     * 创建时间
     */
    private ZonedDateTime createdTime;
}