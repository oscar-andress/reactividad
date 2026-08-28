package demo.reactividad.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import demo.reactividad.entity.Menu;

public interface MenuRepository extends ReactiveCrudRepository<Menu, UUID>{

}
