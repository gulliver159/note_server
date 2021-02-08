package net.thumbtack.school.notes.exception;

import lombok.Value;

@Value
public class ServerError {
    ServerErrorCode errorCode;
    String field;
    String message;
}
