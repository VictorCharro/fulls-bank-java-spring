package com.fulls_bank.Fulls_Bank.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulls_bank.Fulls_Bank.AbstractIntegrationTest;
import com.fulls_bank.Fulls_Bank.entities.AppUser;
import com.fulls_bank.Fulls_Bank.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fluxo real de autenticação: cadastro, login, JWT e regras de acesso, sem usuário simulado. */
@WithAnonymousUser
class AuthIntegrationTest extends AbstractIntegrationTest {

    private static final String PASSWORD = "secret-123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void protectedEndpoint_withoutToken_shouldReturn401InJson() throws Exception {
        mockMvc.perform(get("/transaction/{id}", 1L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void register_thenLogin_shouldIssueATokenThatOpensProtectedEndpoints() throws Exception {
        register("ana@example.com");
        String token = login("ana@example.com", PASSWORD);

        mockMvc.perform(get("/transaction/{id}", 999999L).header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
        assertEquals(Role.USER, appUserRepository.findByEmail("ana@example.com").orElseThrow().getRole());
        assertNotEquals(PASSWORD, appUserRepository.findByEmail("ana@example.com").orElseThrow().getPasswordHash());
    }

    @Test
    void register_withExistingEmail_shouldReturn400() throws Exception {
        register("ana@example.com");

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("ANA@example.com", PASSWORD)))
                .andExpect(status().isBadRequest());
        assertEquals(1, appUserRepository.count());
    }

    @Test
    void login_withWrongPasswordOrUnknownEmail_shouldReturn401() throws Exception {
        register("ana@example.com");

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("ana@example.com", "wrong-password")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("nobody@example.com", PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userRole_shouldNotDeleteOrListEverything_butAdminShould() throws Exception {
        register("ana@example.com");
        String userToken = login("ana@example.com", PASSWORD);
        saveAdmin("admin@example.com");
        String adminToken = login("admin@example.com", PASSWORD);

        mockMvc.perform(delete("/client/{id}", 999999L).header(AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mockMvc.perform(get("/client").header(AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/transaction").header(AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/client/{id}", 999999L).header(AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/client").header(AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void invalidTokens_shouldReturn401() throws Exception {
        mockMvc.perform(get("/transaction/{id}", 1L).header(AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());

        String expired = tokenWith(jwtEncoder, Instant.now().minusSeconds(7200), Instant.now().minusSeconds(3600));
        mockMvc.perform(get("/transaction/{id}", 1L).header(AUTHORIZATION, "Bearer " + expired))
                .andExpect(status().isUnauthorized());

        JwtEncoder otherKey = new NimbusJwtEncoder(new ImmutableSecret<>(new SecretKeySpec(
                "another-secret-with-at-least-32-bytes!!".getBytes(StandardCharsets.UTF_8), "HmacSHA256")));
        String forged = tokenWith(otherKey, Instant.now(), Instant.now().plusSeconds(3600));
        mockMvc.perform(get("/transaction/{id}", 1L).header(AUTHORIZATION, "Bearer " + forged))
                .andExpect(status().isUnauthorized());
    }

    private void register(String email) throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(email, PASSWORD)))
                .andExpect(status().isCreated());
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private void saveAdmin(String email) {
        AppUser admin = new AppUser();
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(PASSWORD));
        admin.setRole(Role.ADMIN);
        appUserRepository.save(admin);
    }

    private String credentials(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private String tokenWith(JwtEncoder encoder, Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtService.ISSUER)
                .subject("ana@example.com")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim(JwtService.ROLE_CLAIM, "ADMIN")
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
