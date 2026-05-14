package ie.tcd.smartlock.utils;

public enum StatusCode {
    SUCCESS(0, "Success"),
    SYSTEM_ERROR(500, "Server error"),
    VALIDATION_ERROR(400, "Invalid input parameters"),
    TOKEN_INVALID(401, "Token is invalid or expired");
    //    ACCOUNT_NOT_FOUND(401, "Username does not exist"),
//    EMAIL_NOT_MATCH(402, "Email dose not match the username"),
//    OPERATION_ERROR(501, "后端操作失败"),
//    ACCOUNT_LOCK(403, "账号已锁定"),
//    ACCOUNT_ERROR(404, "账号密码不匹配"),
//    TOKEN_ERROR(405, "token失效");
    private final int code;
    private final String message;

    StatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
