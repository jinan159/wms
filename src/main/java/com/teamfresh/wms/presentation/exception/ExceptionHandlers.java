package com.teamfresh.wms.presentation.exception;

import com.teamfresh.wms.application.exception.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlers {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(value = {
        MethodArgumentNotValidException.class,
        ApplicationException.class
    })
    public ResponseEntity<ErrorResponse> handleApplicationException(Exception e) {
        var response = ResponseEntity.badRequest();

        logger.error(e.getMessage(), e);

        if (e instanceof MethodArgumentNotValidException) {
            return response.body(
                ErrorResponse.from(((MethodArgumentNotValidException) e).getBindingResult())
            );
        }

        return response.body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleServerError(Exception e) {
        logger.error(e.getMessage(), e);

        return ResponseEntity.badRequest()
            .body(new ErrorResponse(e.getMessage()));
    }

    public record ErrorResponse(
        String message
    ) {
        public static ErrorResponse from(BindingResult bindingResult) {
            var fieldError = bindingResult.getFieldError();

            return new ErrorResponse(
                String.format(
                    "Validation Error: %s %s",
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
                )
            );
        }
    }
}
