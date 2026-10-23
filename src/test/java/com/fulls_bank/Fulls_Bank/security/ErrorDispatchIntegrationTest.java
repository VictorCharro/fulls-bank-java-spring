package com.fulls_bank.Fulls_Bank.security;

import com.fulls_bank.Fulls_Bank.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithAnonymousUser;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Servidor HTTP real: erros do Spring MVC (JSON malformado, tipo de mídia não suportado) são resolvidos com
 * {@code sendError} e o contêiner os redireciona para {@code /error}. O MockMvc não faz esse redirecionamento,
 * por isso este teste sobe o servidor. Sem liberar o despacho de erro, o status real viraria 401.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WithAnonymousUser
class ErrorDispatchIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void login_withMalformedJson_shouldReturn400NotAnAuthenticationError() {
        ResponseEntity<String> response = post("/auth/login", MediaType.APPLICATION_JSON, "{not-json");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void login_withUnsupportedMediaType_shouldReturn415() {
        ResponseEntity<String> response = post("/auth/login", MediaType.TEXT_PLAIN, "hello");

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
    }

    @Test
    void protectedEndpoint_withoutToken_shouldStillReturn401() {
        ResponseEntity<String> response = restTemplate.getForEntity("/transaction/1", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void errorEndpoint_requestedDirectly_shouldStayProtected() {
        ResponseEntity<String> response = restTemplate.exchange("/error", HttpMethod.GET, null, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    private ResponseEntity<String> post(String path, MediaType contentType, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        return restTemplate.postForEntity(path, new HttpEntity<>(body, headers), String.class);
    }
}
