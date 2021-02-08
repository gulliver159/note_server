package net.thumbtack.school.notes.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ServerErrors handleFieldValidation(MethodArgumentNotValidException ex) {
        Set<ServerError> serverErrorSet = new HashSet<>();
        if (ex.hasGlobalErrors()) {
            serverErrorSet.add(new ServerError(ServerErrorCode.All_PARAMETERS_CANNOT_BE_NULL, "parameters",
                    ex.getGlobalError().getDefaultMessage()));
        }
        for (FieldError fieldError : ex.getFieldErrors()) {
            String nameField = fieldError.getField();
            ServerErrorCode errorCode = ServerErrorCode.valueOf("INVALID_" + nameField.toUpperCase());
            serverErrorSet.add(new ServerError(errorCode, nameField,
                    fieldError.getDefaultMessage()));
            log.info("Unable to process request, because " + fieldError.getDefaultMessage() +
                    ", value of incoming field " + fieldError.getRejectedValue());
        }
        return new ServerErrors(serverErrorSet);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ServerException.class)
    @ResponseBody
    public ServerErrors handleServerException(ServerException ex) {
        Set<ServerError> serverErrorSet = new HashSet<>();
        serverErrorSet.add(new ServerError(ex.getErrorCode(), ex.getErrorCode().getField(),
                ex.getErrorCode().getErrorMessage()));
        return new ServerErrors(serverErrorSet);
    }
}
