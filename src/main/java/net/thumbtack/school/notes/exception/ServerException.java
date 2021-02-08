package net.thumbtack.school.notes.exception;

import lombok.Value;

@Value
public class ServerException extends Exception {
    ServerErrorCode errorCode;
}
