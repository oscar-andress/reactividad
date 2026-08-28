package demo.reactividad.exception.menu;

import demo.reactividad.exception.MenuException;

public class MenuBadRequestException extends MenuException {

    public MenuBadRequestException(String message, String errorCode) {
        super(message, errorCode);
    }

}
