package demo.reactividad.exception.menu;

import demo.reactividad.exception.MenuException;

public class MenuUnavailableException extends MenuException{

    public MenuUnavailableException(String message, String errorCode) {
        super(message, errorCode);
    }
    
}
