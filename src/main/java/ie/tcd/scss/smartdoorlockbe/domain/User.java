package ie.tcd.scss.smartdoorlockbe.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
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
     * 用户名
     */
    @NotNull(message = "用户名不能为空")
    @Size(max = 50, message = "账号不能超过50位")
    @TableId
    private String username;

    /**
     * 登录密码
     */
    @NotNull(message = "密码不能为空")
    @Size(max = 50, message = "密码不能超过50位")
    private String password;

    /**
     * 邮箱
     */
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式错误")
    private String email;

    /**
     * 电话
     */
    @Size(max = 20, message = "手机号码不能超过20位")
    private String phone;

    /**
     * 创建时间
     */
    @Null(message = "不能修改创建时间")
    private ZonedDateTime createdTime;

    /**
     * 更新时间
     */
    @Null(message = "不能修改更新时间")
    private ZonedDateTime updatedTime;
}