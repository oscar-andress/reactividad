package demo.reactividad.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import demo.reactividad.exception.menu.MenuBadRequestException;
import demo.reactividad.exception.menu.MenuNotFoundException;
import demo.reactividad.exception.menu.MenuUnavailableException;
import demo.reactividad.handler.GlobalExceptionHandler;
import demo.reactividad.handler.MenuRequestHandler;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RouterConfig {

    private final MenuRequestHandler menuRequestHandler;
    private final GlobalExceptionHandler globalExceptionHandler;

    @Bean
    public RouterFunction<ServerResponse> menuRoute() {
        return RouterFunctions.route()
            .path("/api/v1/menu", this::menuRoutes)
            .build();
    }

    public RouterFunction<ServerResponse> menuRoutes() {
        return RouterFunctions.route()
            .GET("/{menuId}", this.menuRequestHandler::getMenu)
            .POST("/", this.menuRequestHandler::createMenu)
            .PUT("/{menuId}", this.menuRequestHandler::updateMenu)
            .DELETE("/{menuId}", this.menuRequestHandler::deleteMenu)
            .onError(MenuNotFoundException.class, this.globalExceptionHandler::handleMenuNotFoundException)
            .onError(MenuUnavailableException.class, this.globalExceptionHandler::handleMenuUnavailableException)
            .onError(MenuBadRequestException.class, this.globalExceptionHandler::handleMenuBadRequestException)
            .build();
    }
    
}
