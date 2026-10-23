package com.fulls_bank.Fulls_Bank;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationPropertiesTest {

    private final Properties properties = load();

    private static Properties load() {
        try {
            return PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private MockEnvironment environment() {
        MockEnvironment environment = new MockEnvironment();
        environment.getPropertySources().addFirst(new PropertiesPropertySource("application", properties));
        return environment;
    }

    @Test
    void datasourcePassword_shouldNotBeHardcoded() {
        assertEquals("${DB_PASSWORD}", properties.getProperty("spring.datasource.password"));
    }

    @Test
    void datasourcePassword_shouldResolveFromDbPasswordVariable() {
        MockEnvironment environment = environment().withProperty("DB_PASSWORD", "test-password");

        assertEquals("test-password", environment.getProperty("spring.datasource.password"));
    }

    @Test
    void datasourceUrlAndUser_shouldResolveDocumentedDefaults() {
        MockEnvironment environment = environment();

        assertEquals("jdbc:postgresql://localhost:5432/fulls_bank", environment.getProperty("spring.datasource.url"));
        assertEquals("postgres", environment.getProperty("spring.datasource.username"));
    }

    @Test
    void testProfile_shouldUseSeparateDatabaseFromDev() throws IOException {
        Properties testProperties = PropertiesLoaderUtils
                .loadProperties(new ClassPathResource("application-test.properties"));
        MockEnvironment environment = new MockEnvironment();
        environment.getPropertySources().addFirst(new PropertiesPropertySource("application-test", testProperties));

        assertEquals("jdbc:postgresql://localhost:5432/fulls_bank_test",
                environment.getProperty("spring.datasource.url"));
    }

    @Test
    void datasourceUrlAndUser_shouldBeOverriddenByDbUrlAndDbUser() {
        MockEnvironment environment = environment()
                .withProperty("DB_URL", "jdbc:postgresql://db:5432/other")
                .withProperty("DB_USER", "app");

        assertEquals("jdbc:postgresql://db:5432/other", environment.getProperty("spring.datasource.url"));
        assertEquals("app", environment.getProperty("spring.datasource.username"));
    }
}
