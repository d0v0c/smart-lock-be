package ie.tcd.smartlock.model.vo.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 登录/刷新接口返回值，包含一对长短 JWT
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "登录响应", description = "包含 access token 和 refresh token")
public class LoginRespVO {
    @Schema(description = "短 token")
    private String accessToken;
    @Schema(description = "长 token")
    private String refreshToken;
}
