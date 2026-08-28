package demo.reactividad.exception;

public class MenuException extends RuntimeException {
    private final String errorCode;

    protected MenuException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return this.errorCode;
    }
}
