package demo.reactividad;

import java.util.UUID;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class AbstractIntegrationTest {

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        String databaseName = "testdb-" + UUID.randomUUID();
        registry.add("spring.r2dbc.url",
                () -> "r2dbc:h2:mem:///%s;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE".formatted(databaseName));
        registry.add("spring.r2dbc.username", () -> "sa");
        registry.add("spring.r2dbc.password", () -> "");
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.data-locations",
                () -> "classpath:db/schema-h2.sql,classpath:db/test-data.sql");
    }
}
