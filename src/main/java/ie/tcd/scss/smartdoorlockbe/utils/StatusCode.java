package ie.tcd.scss.smartdoorlockbe.utils;

public enum StatusCode {
    SUCCESS(0, "成功"),
    SYSTEM_ERROR(500, "服务器异常"),
    VALIDATION_ERROR(400, "传入参数错误"),
    ACCOUNT_ALREADY_EXISTS(401, "账号已存在"),
    OPERATION_ERROR(501, "后端操作失败"),
    ACCOUNT_NOT_FOUND(402, "账号不存在"),
    ACCOUNT_LOCK(403, "账号已锁定"),
    ACCOUNT_ERROR(404, "账号密码不匹配"),
    TOKEN_ERROR(405, "token失效");
    private final int code;
    private final String message;

    StatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {return code;}

    public String getMessage() {return message;}
}
