package analu.whereio.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final LocalDateTime timestamp;

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}
