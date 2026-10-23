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
    void devProfile_shouldShowSql() {
        ConfigurableEnvironment environment = environmentFor("--spring.profiles.active=dev");

        assertEquals("true", environment.getProperty("spring.jpa.show-sql"));
        assertEquals("true", environment.getProperty("spring.jpa.properties.hibernate.format_sql"));
    }

    @Test
    void testProfile_shouldKeepLogQuiet() {
        ConfigurableEnvironment environment = environmentFor("--spring.profiles.active=test");

        assertEquals("false", environment.getProperty("spring.jpa.show-sql"));
    }

    @Test
    void allProfiles_shouldOnlyValidateSchemaCreatedByFlyway() {
        for (String profile : new String[]{"dev", "test"}) {
            ConfigurableEnvironment environment = environmentFor("--spring.profiles.active=" + profile);

            assertEquals("validate", environment.getProperty("spring.jpa.hibernate.ddl-auto"));
        }
    }

    @Test
    void allProfiles_shouldShareTheDatasourceFromEnvironmentVariables() {
        ConfigurableEnvironment environment = environmentFor(
                "--spring.profiles.active=test",
                "--TEST_DB_URL=jdbc:postgresql://db:5432/other_test");

        assertEquals("jdbc:postgresql://db:5432/other_test", environment.getProperty("spring.datasource.url"));
        assertEquals("test-password", environment.getProperty("spring.datasource.password"));
        assertEquals("test-user", environment.getProperty("spring.datasource.username"));
    }
}
