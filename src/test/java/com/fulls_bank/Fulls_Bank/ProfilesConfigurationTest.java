package com.fulls_bank.Fulls_Bank;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfilesConfigurationTest {

    @Configuration
    static class EmptyConfig {
    }

    private ConfigurableEnvironment environmentFor(String profile) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(EmptyConfig.class)
                .web(WebApplicationType.NONE)
                .run("--spring.profiles.active=" + profile, "--DB_PASSWORD=test-password");
        try {
            return context.getEnvironment();
        } finally {
            context.close();
        }
    }

    @Test
    void activeProfile_shouldDefaultToDevAndBeConfigurableByEnvironmentVariable() throws IOException {
        String value = PropertiesLoaderUtils
                .loadProperties(new ClassPathResource("application.properties"))
                .getProperty("spring.profiles.active");

        assertEquals("${SPRING_PROFILES_ACTIVE:dev}", value);
    }

    @Test
    void devProfile_shouldUpdateSchemaAndShowSql() {
        ConfigurableEnvironment environment = environmentFor("dev");

        assertEquals("update", environment.getProperty("spring.jpa.hibernate.ddl-auto"));
        assertEquals("true", environment.getProperty("spring.jpa.show-sql"));
        assertEquals("true", environment.getProperty("spring.jpa.properties.hibernate.format_sql"));
    }

    @Test
    void testProfile_shouldRecreateSchemaAndKeepLogQuiet() {
        ConfigurableEnvironment environment = environmentFor("test");

        assertEquals("create-drop", environment.getProperty("spring.jpa.hibernate.ddl-auto"));
        assertEquals("false", environment.getProperty("spring.jpa.show-sql"));
    }

    @Test
    void allProfiles_shouldShareTheDatasourceFromEnvironmentVariables() {
        ConfigurableEnvironment environment = environmentFor("test");

        assertEquals("test-password", environment.getProperty("spring.datasource.password"));
        assertEquals("postgres", environment.getProperty("spring.datasource.username"));
    }
}
