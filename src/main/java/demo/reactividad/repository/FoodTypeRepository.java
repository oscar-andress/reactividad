package demo.reactividad.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import demo.reactividad.entity.FoodType;
import reactor.core.publisher.Flux;

public interface FoodTypeRepository extends ReactiveCrudRepository<FoodType, UUID> {
    @Query("""
            SELECT ft.*
            FROM tbl_menu_food_type mft
            INNER JOIN tbl_food_type ft ON ft.food_type_id = mft.food_type_id
            WHERE mft.menu_id = :menuId
            """)
    Flux<FoodType> findByMenuId(UUID menuId);
}
