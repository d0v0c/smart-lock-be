package ie.tcd.smartlock.utils;

/**
 * 防抖拦截抛出。由 GlobalExceptionHandler 转 HTTP 429。
 */
public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException() {
        super(StatusCode.TOO_MANY_REQUESTS.getMessage());
    }
}
