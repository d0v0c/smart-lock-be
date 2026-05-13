package ie.tcd.smartlock.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(title = "响应包装格式", description = "状态码+数据+错误信息")
@Data
public class Result<T> {
    @Schema(description = "状态码 0 表示成功，其他失败")
    private int code;
    @Schema(description = "数据")
    private T data;
    @Schema(description = "错误信息")
    private String message;

    /**
     * 响应成功
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.data = data;
        result.code = StatusCode.SUCCESS.getCode();
        result.message = StatusCode.SUCCESS.getMessage();
        return result;
    }

    /**
     * 响应失败
     */
    public static Result<Void> fail(StatusCode statusCode, String message) {
        Result<Void> result = new Result<>();
        result.code = statusCode.getCode();
        result.message = message;
        return result;
    }

    // 重载 fail 方法
    public static Result<Void> fail(StatusCode statusCode) {
        // 调用重载方法，使用 StatusCode 默认消息
        return fail(statusCode, statusCode.getMessage());
    }
}
