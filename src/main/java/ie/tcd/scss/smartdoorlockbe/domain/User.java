package ie.tcd.scss.smartdoorlockbe.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import ie.tcd.scss.smartdoorlockbe.domain.validation.RegisterGroup;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.ZonedDateTime;

/**
 * 后台用户表
 *
 * @TableName user
 */
@TableName(value = "user")
@Data
public class User {
    /**
     * 用户名（不能修改）
     */
    @NotNull(groups = RegisterGroup.class, message = "username cannot be null")
    @Size(max = 50, message = "username cannot exceed 50 characters")
    @TableId
    private String username;

    /**
     * 登录密码
     */
    @NotNull(groups = RegisterGroup.class, message = "password cannot be null")
    @Size(max = 50, message = "password cannot exceed 50 characters")
    private String password;

    /**
     * 邮箱
     */
    @NotNull(groups = RegisterGroup.class, message = "email cannot be null")
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Invalid email address")
    private String email;

    /**
     * 电话
     */
    @Pattern(regexp = "^(\\+?\\d{1,4})?\\d{7,15}$", message = "Invalid phone number")
    private String phone;

    /**
     * 创建时间
     */
    @Null(message = "Cannot modify the createdTime")
    private ZonedDateTime createdTime;

    /**
     * 更新时间
     */
    @Null(message = "Cannot modify the updatedTime")
    private ZonedDateTime updatedTime;
}