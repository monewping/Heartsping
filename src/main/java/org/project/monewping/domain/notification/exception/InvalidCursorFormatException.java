package org.project.monewping.domain.notification.exception;

public class InvalidCursorFormatException extends RuntimeException {

    private final String cursor;

    public InvalidCursorFormatException(String cursor, Throwable cause) {
        super("잘못된 커서 형식입니다. 입력값: " + cursor, cause);
        this.cursor = cursor;
    }

    public String getCursor() {
        return cursor;
    }
}
