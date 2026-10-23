package com.fulls_bank.Fulls_Bank;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfilesConfigurationTest {

    @Configuration
    static class EmptyConfig {
    }

    private ConfigurableEnvironment environmentFor(String... arguments) {
        String[] applicationArguments = Stream.concat(
                Arrays.stream(arguments),
                Stream.of("--DB_PASSWORD=test-password", "--DB_USER=test-user")
        ).toArray(String[]::new);
        ConfigurableApplicationContext context = new SpringApplicationBuilder(EmptyConfig.class)
                .web(WebApplicationType.NONE)
                .run(applicationArguments);
        try {
            return context.getEnvironment();
        } finally {
            context.close();
        }
    }

    @Test
    void activeProfile_withoutConfiguration_shouldBeDev() {
        Assumptions.assumeTrue(System.getenv("SPRING_PROFILES_ACTIVE") == null,
                "SPRING_PROFILES_ACTIVE está definida no ambiente");

        ConfigurableEnvironment environment = environmentFor();

        assertArrayEquals(new String[]{"dev"}, environment.getActiveProfiles());
    }

    @Test
    void activeProfile_shouldBeConfigurableBySpringProfilesActiveVariable() {
        ConfigurableEnvironment environment = environmentFor("--SPRING_PROFILES_ACTIVE=test");

        assertArrayEquals(new String[]{"test"}, environment.getActiveProfiles());
    }

    @Test
    void devProfile_shouldUpdateSchemaAndShowSql() {
        ConfigurableEnvironment environment = environmentFor("--spring.profiles.active=dev");

        assertEquals("update", environment.getProperty("spring.jpa.hibernate.ddl-auto"));
        assertEquals("true", environment.getProperty("spring.jpa.show-sql"));
        assertEquals("true", environment.getProperty("spring.jpa.properties.hibernate.format_sql"));
    }

    @Test
    void testProfile_shouldRecreateSchemaAndKeepLogQuiet() {
        ConfigurableEnvironment environment = environmentFor("--spring.profiles.active=test");

        assertEquals("create-drop", environment.getProperty("spring.jpa.hibernate.ddl-auto"));
        assertEquals("false", environment.getProperty("spring.jpa.show-sql"));
    }

    @Test
    void allProfiles_shouldShareTheDatasourceFromEnvironmentVariables() {
        ConfigurableEnvironment environment = environmentFor("--spring.profiles.active=test");

        assertEquals("test-password", environment.getProperty("spring.datasource.password"));
        assertEquals("test-user", environment.getProperty("spring.datasource.username"));
    }
}
