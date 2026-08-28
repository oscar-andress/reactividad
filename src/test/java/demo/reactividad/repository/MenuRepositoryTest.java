package demo.reactividad.repository;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import demo.reactividad.AbstractIntegrationTest;
import reactor.test.StepVerifier;

@SpringBootTest
public class MenuRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private MenuRepository menuRepository;
    private static final Logger log = LoggerFactory.getLogger(MenuRepositoryTest.class);

    @Test
    public void findAll_Sucess() {
        this.menuRepository.findAll()
                        .doOnNext(m -> log.info("{}", m))
                        .as(StepVerifier::create)
                        .expectNextCount(1)
                        .expectComplete()
                        .verify();
    }

    @Test
    public void findById_Sucess() {
        this.menuRepository.findById(UUID.fromString("a0814b88-87c3-4c56-a08e-c95c92fc7894"))
                        .doOnNext(m -> log.info("{}", m))
                        .as(StepVerifier::create)
                        .assertNext(m -> Assertions.assertEquals("DEVOS", m.getTitle()))
                        .expectComplete()
                        .verify();
    }

    @Test
    public void updateMenu_Success() {
        this.menuRepository.findById(UUID.fromString("a0814b88-87c3-4c56-a08e-c95c92fc7894"))
                           .doOnNext(m -> m.setDescription("Lorem Ipsum"))
                           .flatMap(m -> this.menuRepository.save(m))
                           .doOnNext(m -> log.info("{}", m))
                           .as(StepVerifier::create)
                           .assertNext(c -> Assertions.assertEquals("Lorem Ipsum", c.getDescription()))
                           .expectComplete()
                           .verify();
    }
}
