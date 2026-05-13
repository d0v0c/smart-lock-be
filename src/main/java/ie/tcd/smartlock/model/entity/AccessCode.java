package ie.tcd.smartlock.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 门锁密码表
 *
 * @TableName access_code
 */
@TableName(value = "access_code")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private LocalDateTime validFrom;

    /**
     * 有效期结束时间
     */
    private LocalDateTime validTo;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}