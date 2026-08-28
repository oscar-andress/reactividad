package demo.reactividad.exception.menu;

import demo.reactividad.exception.MenuException;

public class MenuNotFoundException extends MenuException{

    public MenuNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }
    
}
