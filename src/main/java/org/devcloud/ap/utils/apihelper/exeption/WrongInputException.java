package org.devcloud.ap.utils.apihelper.exeption;

public class WrongInputException extends Exception {
    public WrongInputException(String errorMessage) {
        super(errorMessage);
    }
}
