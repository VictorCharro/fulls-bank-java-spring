package com.fulls_bank.Fulls_Bank;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationPropertiesTest {

    private final Properties properties = load();

    private static Properties load() {
        try {
            return PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void datasourcePassword_shouldComeFromEnvironmentVariable() {
        assertEquals("${DB_PASSWORD}", properties.getProperty("spring.datasource.password"));
    }

    @Test
    void datasourceUrlAndUser_shouldBeOverridableByEnvironmentVariables() {
        assertTrue(properties.getProperty("spring.datasource.url").startsWith("${DB_URL:"));
        assertTrue(properties.getProperty("spring.datasource.username").startsWith("${DB_USER:"));
    }
}
