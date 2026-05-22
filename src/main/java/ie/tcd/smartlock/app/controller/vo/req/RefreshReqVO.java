package ie.tcd.smartlock.app.controller.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @description: /api/user/refresh 接口的请求 body
 */
@Data
@Schema(title = "刷新 token 请求")
public class RefreshReqVO {
    @Schema(description = "有效的 refresh token")
    @NotBlank(message = "refreshToken must not be blank")
    private String refreshToken;
}
