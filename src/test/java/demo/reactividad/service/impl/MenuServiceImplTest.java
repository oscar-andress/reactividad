package demo.reactividad.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import demo.reactividad.dto.request.MenuCreateRequestDTO;
import demo.reactividad.dto.request.MenuUpdateRequestDTO;
import demo.reactividad.dto.response.MenuResponseDTO;
import demo.reactividad.entity.Menu;
import demo.reactividad.exception.menu.MenuNotFoundException;
import demo.reactividad.exception.menu.MenuUnavailableException;
import demo.reactividad.mapper.FoodTypeMapper;
import demo.reactividad.mapper.MenuMapper;
import demo.reactividad.repository.FoodTypeRepository;
import demo.reactividad.repository.MenuRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

    @Mock
    private MenuRepository menuRepository;
    @Mock
    private FoodTypeRepository foodTypeRepository;
    @Mock
    private FoodTypeMapper foodTypeMapper;
    @Mock
    private MenuMapper menuMapper;

    @InjectMocks
    private MenuServiceImpl menuService;

    @Test
    void getMenu_whenMenuDoesNotExist_throwsMenuNotFoundException() {
        UUID menuId = UUID.randomUUID();
        when(menuRepository.findById(menuId)).thenReturn(Mono.empty());

        StepVerifier.create(menuService.getMenu(menuId))
                .expectError(MenuNotFoundException.class)
                .verify();
    }

    @Test
    void getMenu_whenMenuExists_returnsMenuWithFoodTypes() {
        UUID menuId = UUID.randomUUID();
        Menu menu = new Menu("Title", "Description");
        MenuResponseDTO expected = new MenuResponseDTO(menuId, "Title", "Description", LocalDateTime.now(), Set.of());

        when(menuRepository.findById(menuId)).thenReturn(Mono.just(menu));
        when(foodTypeRepository.findByMenuId(menuId)).thenReturn(Flux.empty());
        when(menuMapper.toMenuResponseDTO(menu, Set.of())).thenReturn(expected);

        StepVerifier.create(menuService.getMenu(menuId))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void createMenu_mapsAndSavesMenu() {
        MenuCreateRequestDTO request = new MenuCreateRequestDTO("Title", "Description");
        Menu menuToSave = new Menu("Title", "Description");
        Menu savedMenu = new Menu("Title", "Description");
        MenuResponseDTO expected = new MenuResponseDTO(UUID.randomUUID(), "Title", "Description", LocalDateTime.now(), Set.of());

        when(menuMapper.toMenu(request)).thenReturn(menuToSave);
        when(menuRepository.save(menuToSave)).thenReturn(Mono.just(savedMenu));
        when(menuMapper.toMenuResponseDTO(savedMenu, Set.of())).thenReturn(expected);

        StepVerifier.create(menuService.createMenu(request))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void updateMenu_whenMenuExists_updatesSuccessfully() {
        UUID menuId = UUID.randomUUID();
        MenuUpdateRequestDTO request = new MenuUpdateRequestDTO("New Title", "New Description");
        Menu existingMenu = new Menu("Old Title", "Old Description");
        Menu savedMenu = new Menu("New Title", "New Description");
        MenuResponseDTO expected = new MenuResponseDTO(menuId, "New Title", "New Description", LocalDateTime.now(), Set.of());

        when(menuRepository.findById(menuId)).thenReturn(Mono.just(existingMenu));
        when(menuRepository.save(existingMenu)).thenReturn(Mono.just(savedMenu));
        when(foodTypeRepository.findByMenuId(menuId)).thenReturn(Flux.empty());
        when(menuMapper.toMenuResponseDTO(savedMenu, Set.of())).thenReturn(expected);

        StepVerifier.create(menuService.updateMenu(menuId, request))
                .expectNext(expected)
                .verifyComplete();

        verify(menuMapper).updateMenuFields(existingMenu, request);
    }

    @Test
    void updateMenu_whenMenuDoesNotExist_throwsMenuNotFoundException() {
        UUID menuId = UUID.randomUUID();
        MenuUpdateRequestDTO request = new MenuUpdateRequestDTO("New Title", "New Description");
        when(menuRepository.findById(menuId)).thenReturn(Mono.empty());

        StepVerifier.create(menuService.updateMenu(menuId, request))
                .expectError(MenuNotFoundException.class)
                .verify();

        verify(menuRepository, never()).save(any(Menu.class));
    }

    @Test
    void deleteMenu_whenMenuExists_deletesSuccessfully() {
        UUID menuId = UUID.randomUUID();
        when(menuRepository.existsById(menuId)).thenReturn(Mono.just(true));
        when(menuRepository.deleteById(menuId)).thenReturn(Mono.empty());

        StepVerifier.create(menuService.deleteMenu(menuId))
                .verifyComplete();

        verify(menuRepository).deleteById(menuId);
    }

    @Test
    void deleteMenu_whenMenuDoesNotExist_throwsMenuNotFoundExceptionWithoutDeleting() {
        UUID menuId = UUID.randomUUID();
        when(menuRepository.existsById(menuId)).thenReturn(Mono.just(false));

        StepVerifier.create(menuService.deleteMenu(menuId))
                .expectError(MenuNotFoundException.class)
                .verify();

        verify(menuRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void fallbackGetMenu_returnsMenuUnavailableException() {
        UUID menuId = UUID.randomUUID();

        StepVerifier.create(menuService.fallbackGetMenu(menuId, new RuntimeException("circuit open")))
                .expectError(MenuUnavailableException.class)
                .verify();
    }
}
