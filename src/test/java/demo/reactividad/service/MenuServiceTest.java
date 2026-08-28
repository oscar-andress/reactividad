package demo.reactividad.service;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import demo.reactividad.AbstractIntegrationTest;
import demo.reactividad.dto.request.MenuCreateRequestDTO;

@AutoConfigureWebTestClient
@SpringBootTest
public class MenuServiceTest extends AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(MenuServiceTest.class);
    
    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void getMenu_Success() {
        this.webTestClient.get()
                .uri("/api/v1/menu/a0814b88-87c3-4c56-a08e-c95c92fc7894")
                .header("auth-token", "secret123")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
            .consumeWith(r -> log.info("{}", new String(r.getResponseBody())))
            .jsonPath("$.menuId").isEqualTo("a0814b88-87c3-4c56-a08e-c95c92fc7894");
    }

    @Test
    public void getMenu_Unauthorized() {
        this.webTestClient.get()
                .uri("/api/v1/menu/a0814b88-87c3-4c56-a08e-c95c92fc7894")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void getMenu_NotFound() {
        this.webTestClient.get()
                .uri("/api/v1/menu/a0814b88-87c3-4c56-a08e-c95c92fc7893")
                .header("auth-token", "secret123")
                .exchange()
                .expectStatus().is4xxClientError()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
            .consumeWith(r -> log.info("{}", new String(r.getResponseBody())))
            .jsonPath("$.message").isEqualTo("Menu with id a0814b88-87c3-4c56-a08e-c95c92fc7893 not found");
    }

    @Test
    public void postMenu_Sucess() {
        MenuCreateRequestDTO menu = new MenuCreateRequestDTO("Test", "Test description");
        this.webTestClient.post()
                .uri("/api/v1/menu/")
                .header("auth-token", "secret456")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(menu)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
            .consumeWith(r -> log.info("{}", new String(r.getResponseBody())))
            .jsonPath("$.menuTitle").isEqualTo(menu.menuTitle());
    }

    @Test
    public void postMenu_Forbbiden() {
        MenuCreateRequestDTO menu = new MenuCreateRequestDTO("Test", "Test description");
        this.webTestClient.post()
                .uri("/api/v1/menu/")
                .header("auth-token", "secret123")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(menu)
                .exchange()
                .expectStatus().isForbidden();
    }

}
