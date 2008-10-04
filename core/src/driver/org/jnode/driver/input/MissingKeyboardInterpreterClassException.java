package org.jnode.driver.input;

public class MissingKeyboardInterpreterClassException extends KeyboardInterpreterException {

    public MissingKeyboardInterpreterClassException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingKeyboardInterpreterClassException(String message) {
        super(message);
    }

}
