package org.devcloud.ap.utils.helper.exeption;

public class WrongInputException extends Exception {
    public WrongInputException(String errorMessage) {
        super(errorMessage);
    }
}
