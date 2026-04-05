package analu.whereio.exceptions;

import analu.whereio.exceptions.handler.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {

    ErrorResponse error = new ErrorResponse(
            ex.getStatus().value(),
            ex.getMessage()
    );

    return new ResponseEntity<>(error, ex.getStatus());
  }
}