package ie.tcd.scss.smartdoorlockbe.utils;

public class BusinessException extends RuntimeException {
    private final StatusCode statusCode;
    private final String message;

    public BusinessException(StatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.message = message;
    }

    public StatusCode getStatusCode() {return statusCode;}

    @Override
    public String getMessage() {return message;}
}
