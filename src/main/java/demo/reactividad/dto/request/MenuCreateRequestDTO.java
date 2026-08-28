package demo.reactividad.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MenuCreateRequestDTO (
    @NotBlank(message = "menuTitle must not be blank")
    @Size(max = 50, message = "menuTitle must be at most 50 characters")
    String menuTitle,

    @NotBlank(message = "menuDescription must not be blank")
    @Size(max = 50, message = "menuDescription must be at most 50 characters")
    String menuDescription
) {

}
