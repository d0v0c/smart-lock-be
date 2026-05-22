package ie.tcd.smartlock.app.controller.vo.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author xylingying
 * @date 2025-03-10 22:14
 * @description: 擦除密码
 */
@Data
public class UserInfoRespVO {
    private String username;
    private String email;
    private String phone;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
