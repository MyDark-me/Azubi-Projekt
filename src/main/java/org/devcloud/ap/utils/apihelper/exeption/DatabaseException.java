package org.devcloud.ap.utils.apihelper.exeption;

public class DatabaseException extends Exception {
    public DatabaseException(String errorMessage) {
        super(errorMessage);
    }
}
