package ie.tcd.scss.smartdoorlockbe.vo.resp;

import lombok.Data;

import java.time.ZonedDateTime;

/**
 * @author xylingying
 * @date 2025-03-10 22:14
 * @description: 返回邮箱密码
 */
@Data
public class UserInfoRespVO {
    private String username;
    private String email;
    private String phone;
    private ZonedDateTime createdTime;
    private ZonedDateTime updatedTime;
}
