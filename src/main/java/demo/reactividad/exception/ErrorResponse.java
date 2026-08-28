package demo.reactividad.exception;

import java.time.LocalDateTime;

public record ErrorResponse (
    LocalDateTime timestamp,
    int status,
    String message,
    String errorCode,
    String path
) {

}
