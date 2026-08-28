package demo.reactividad.service;

import java.util.UUID;

import demo.reactividad.dto.request.MenuCreateRequestDTO;
import demo.reactividad.dto.response.MenuResponseDTO;
import reactor.core.publisher.Mono;

public interface MenuService {
    Mono<MenuResponseDTO> getMenu(UUID menuId);
    Mono<MenuResponseDTO> createMenu(MenuCreateRequestDTO request);
    Mono<Void> deleteMenu(UUID menuId);
}
