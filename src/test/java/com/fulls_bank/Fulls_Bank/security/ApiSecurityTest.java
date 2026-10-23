package com.fulls_bank.Fulls_Bank.security;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Para testes {@code @WebMvcTest}: traz a configuração de segurança real (regras de acesso e respostas
 * 401/403 em JSON) com um segredo JWT exclusivo de teste. O usuário da requisição é escolhido em cada teste
 * (por exemplo com {@code @WithMockUser}).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({SecurityConfig.class, RestSecurityErrorHandler.class})
@TestPropertySource(properties = "app.jwt.secret=test-only-jwt-secret-with-at-least-32-bytes")
public @interface ApiSecurityTest {
}
