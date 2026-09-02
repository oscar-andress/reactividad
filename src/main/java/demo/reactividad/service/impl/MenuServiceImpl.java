package demo.reactividad.service.impl;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import demo.reactividad.dto.request.MenuCreateRequestDTO;
import demo.reactividad.dto.request.MenuUpdateRequestDTO;
import demo.reactividad.dto.response.MenuResponseDTO;
import demo.reactividad.enums.MenuCodeException;
import demo.reactividad.exception.menu.MenuNotFoundException;
import demo.reactividad.exception.menu.MenuUnavailableException;
import demo.reactividad.mapper.FoodTypeMapper;
import demo.reactividad.mapper.MenuMapper;
import demo.reactividad.repository.FoodTypeRepository;
import demo.reactividad.repository.MenuRepository;
import demo.reactividad.service.MenuService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class MenuServiceImpl implements MenuService{

    private final MenuRepository menuRepository;
    private final FoodTypeRepository foodTypeRepository;
    private final FoodTypeMapper foodTypeMapper;
    private final MenuMapper menuMapper;

    @Override
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "menu-service-reactivo", fallbackMethod = "fallbackGetMenu")
    public Mono<MenuResponseDTO> getMenu(UUID menuId) {

        return menuRepository.findById(menuId)
            .switchIfEmpty(Mono.error( () -> new MenuNotFoundException("Menu with id "+ menuId + " not found", 
                                                                 MenuCodeException.NOT_FOUND.name())))
            .flatMap(menu -> {
                return foodTypeRepository.findByMenuId(menuId)
                    .map(foodType -> foodTypeMapper.toFoodTypeResponseDTO(foodType))
                    .collect(Collectors.toSet())
                    .map(foodTypeDtos -> menuMapper.toMenuResponseDTO(menu, foodTypeDtos));
                    
            });
            
    }

    public Mono<MenuResponseDTO> fallbackGetMenu(UUID menuId, Throwable ex) {
        return Mono.error(new MenuUnavailableException("Service unavailable",
                                                      MenuCodeException.UNAVAILABLE.name()));
    }

    @Override
    public Mono<MenuResponseDTO> createMenu(MenuCreateRequestDTO request) {
        return Mono.just(request)
                .map(r -> menuMapper.toMenu(r))
                .flatMap(menu -> menuRepository.save(menu))
                .map(response -> menuMapper.toMenuResponseDTO(response, Set.of()));
    }

    @Override
    public Mono<MenuResponseDTO> updateMenu(UUID menuId, MenuUpdateRequestDTO request) {
        return menuRepository.findById(menuId)
            .switchIfEmpty(Mono.error( () -> new MenuNotFoundException("Menu with id "+ menuId + " not found",
                                                                 MenuCodeException.NOT_FOUND.name())))
            .flatMap(menu -> {
                menuMapper.updateMenuFields(menu, request);
                return menuRepository.save(menu);
            })
            .flatMap(saved -> foodTypeRepository.findByMenuId(menuId)
                .map(foodType -> foodTypeMapper.toFoodTypeResponseDTO(foodType))
                .collect(Collectors.toSet())
                .map(foodTypeDtos -> menuMapper.toMenuResponseDTO(saved, foodTypeDtos)));
    }

    @Override
    public Mono<Void> deleteMenu(UUID menuId) {
        return this.menuRepository.existsById(menuId)
                   .flatMap(exists -> exists
                       ? this.menuRepository.deleteById(menuId)
                       : Mono.error(new MenuNotFoundException("Menu with id "+ menuId + " not found",
                                                                 MenuCodeException.NOT_FOUND.name())));
    }
    
}
