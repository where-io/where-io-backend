package analu.whereio.exceptions.handler;

import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
public class ValidationErrorResponse {

    private final int status;
    private final String message;
    private final Map<String, String> fieldErrors;
    private final Instant timestamp;

    public ValidationErrorResponse(Map<String, String> fieldErrors) {
        this.status = 400;
        this.message = "Erro de validação";
        this.fieldErrors = Map.copyOf(fieldErrors);
        this.timestamp = Instant.now();
    }
}
