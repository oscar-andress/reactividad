package demo.reactividad.mapper;

import org.springframework.stereotype.Component;

import demo.reactividad.dto.response.FoodTypeResponseDTO;
import demo.reactividad.entity.FoodType;

@Component
public class FoodTypeMapper {
    public FoodTypeResponseDTO toFoodTypeResponseDTO(FoodType foodType) {
        return new FoodTypeResponseDTO(
            foodType.getFoodTypeId(),
            foodType.getFoodTypeName(),
            foodType.isActive()
        );
    }
}
