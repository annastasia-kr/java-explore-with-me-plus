package ru.practicum.ewm;

import org.junit.jupiter.api.AfterAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@SuppressWarnings({"resource", "unused"}) // Игнорируем предупреждение о ресурсе
public abstract class BaseIntegrationTest {

    static PostgreSQLContainer<?> postgreSQLContainer; // Контейнер должен жить долго

    static {
        postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");

        postgreSQLContainer.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // Этот метод ВЫЗЫВАЕТСЯ Spring'ом, предупреждение можно игнорировать
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

        // Добавляем дополнительные свойства
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
    }

    @AfterAll
    static void afterAll() {
        // Останавливаем контейнер после всех тестов
        if (postgreSQLContainer != null && postgreSQLContainer.isRunning()) {
            postgreSQLContainer.stop();
        }
    }
}