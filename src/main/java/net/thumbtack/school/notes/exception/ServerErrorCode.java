package net.thumbtack.school.notes.exception;

public enum ServerErrorCode {
    INVALID_FIRSTNAME,
    INVALID_LASTNAME,
    INVALID_PATRONYMIC,
    INVALID_LOGIN,
    INVALID_PASSWORD,
    INVALID_NAME,
    INVALID_SUBJECT,
    INVALID_BODY,
    INVALID_SECTIONID,
    INVALID_NOTEID,
    INVALID_RATING,
    All_PARAMETERS_CANNOT_BE_NULL,


    LOGIN_ALREADY_BUSY("This login is already busy", "login"),
    WRONG_PASSWORD("Invalid password passed", "password"),
    THIS_SESSIONID_NOT_FOUND("The session id passed was not found", "sessionId"),
    THIS_LOGIN_AND_PASSWORD_NOT_FOUND("A User with this login and password was not found", "login and password"),
    THIS_LOGIN_NOT_FOUND("A User with this login was not found", "login"),
    THIS_ID_NOT_FOUND("A User with this id was not found", "userId"),
    YOU_ARE_NOT_SUPERUSER("You must be a superuser to perform this operation", "userType"),
    SESSION_TIME_IS_OVER("Too long inactivity, automatic logout occurred", "sessionId"),

    WRONG_SEARCH_PARAM("Invalid search parameter passed", "searchParameter"),
    INVALID_PARAM_VALUE("Invalid parameter value passed", "valueOfSearchParameter"),

    SECTION_NAME_ALREADY_BUSY("This section name is already busy", "name"),
    THIS_SECTION_ID_NOT_FOUND("This section id was not found", "sectionId"),
    YOU_ARE_NOT_OWNER_OF_SECTION("You are not the owner of the section", "sectionId"),

    THIS_NOTE_ID_NOT_FOUND("This note id was not found", "noteId"),
    YOU_ARE_NOT_OWNER_OF_NOTE("You are not the owner of the note", "noteId"),

    THIS_COMMENT_ID_NOT_FOUND("This comment id was not found", "commentId"),
    YOU_ARE_NOT_OWNER_OF_COMMENT("You are not the owner of the comment", "commentId"),
    YOU_ARE_NOT_OWNER_OF_COMMENT_OR_NOTE("You are not the owner of the comment " +
            "or the note to which the comment relates", "commentId"),
    YOU_CANT_RATE_YOUR_NOTE("You can't rate your own note", "noteId");

    private String errorMessage;
    private String field;

    ServerErrorCode(String errorMessage, String field) {
        this.errorMessage = errorMessage;
        this.field = field;
    }

    ServerErrorCode() {
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getField() {
        return field;
    }
}
