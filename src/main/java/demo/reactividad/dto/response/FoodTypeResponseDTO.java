package demo.reactividad.dto.response;

import java.util.UUID;

public record FoodTypeResponseDTO (
    UUID foodTypeId,
    String foodTypeName,
    boolean active
) {

}
