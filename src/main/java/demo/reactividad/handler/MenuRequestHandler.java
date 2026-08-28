package demo.reactividad.handler;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import demo.reactividad.dto.request.MenuCreateRequestDTO;
import demo.reactividad.enums.MenuCodeException;
import demo.reactividad.exception.menu.MenuBadRequestException;
import demo.reactividad.service.MenuService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MenuRequestHandler {
    private final MenuService menuService;
    private final Validator validator;

    public Mono<ServerResponse> getMenu (ServerRequest request) {
        return parseMenuId(request)
                .flatMap(this.menuService::getMenu)
                .flatMap(r -> ServerResponse.status(HttpStatus.OK).bodyValue(r));

    }

    public Mono<ServerResponse> createMenu (ServerRequest request) {
        return request.bodyToMono(MenuCreateRequestDTO.class)
                      .flatMap(this::validate)
                      .flatMap(dto -> this.menuService.createMenu(dto))
                      .flatMap(responseDto -> ServerResponse.status(HttpStatus.CREATED).bodyValue(responseDto));

    }

    public Mono<ServerResponse> deleteMenu (ServerRequest request) {
        return parseMenuId(request)
                .flatMap(this.menuService::deleteMenu)
                .then(ServerResponse.noContent().build());
    }

    private Mono<UUID> parseMenuId(ServerRequest request) {
        return Mono.defer(() -> {
            String menuId = request.pathVariable("menuId");
            try {
                return Mono.just(UUID.fromString(menuId));
            } catch (IllegalArgumentException ex) {
                return Mono.error(new MenuBadRequestException(
                        "Invalid menuId: " + menuId, MenuCodeException.BAD_REQUEST.name()));
            }
        });
    }

    private Mono<MenuCreateRequestDTO> validate(MenuCreateRequestDTO dto) {
        Set<ConstraintViolation<MenuCreateRequestDTO>> violations = validator.validate(dto);
        if (violations.isEmpty()) {
            return Mono.just(dto);
        }
        String message = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        return Mono.error(new MenuBadRequestException(message, MenuCodeException.BAD_REQUEST.name()));
    }
}
