package demo.reactividad.mapper;

import java.util.Set;

import org.springframework.stereotype.Component;

import demo.reactividad.dto.request.MenuCreateRequestDTO;
import demo.reactividad.dto.response.FoodTypeResponseDTO;
import demo.reactividad.dto.response.MenuResponseDTO;
import demo.reactividad.entity.Menu;

@Component
public class MenuMapper {
    public MenuResponseDTO toMenuResponseDTO(Menu menu, Set<FoodTypeResponseDTO> foodTypes){
        return new MenuResponseDTO(
            menu.getId(), 
            menu.getTitle(), 
            menu.getDescription(), 
            menu.getCreatedAt(),
            foodTypes);
    }

    public Menu toMenu(MenuCreateRequestDTO request) {
        return new Menu(
            request.menuTitle(), 
            request.menuDescription());
    }
}
