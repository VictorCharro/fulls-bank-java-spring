package com.fulls_bank.Fulls_Bank.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulls_bank.Fulls_Bank.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Documentação OpenAPI gerada pela aplicação real: pública, completa e coerente com a segurança. */
@WithAnonymousUser
class OpenApiIntegrationTest extends AbstractIntegrationTest {

    private static final Set<String> HTTP_METHODS = Set.of("get", "post", "put", "delete", "patch");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void docs_shouldBePublicWithoutToken() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test
    void docs_shouldDescribeEveryEndpointOfTheApi() throws Exception {
        JsonNode paths = apiDocs().get("paths");

        List<String> expected = List.of(
                "POST /auth/register", "POST /auth/login",
                "POST /client", "GET /client", "GET /client/{id}", "PUT /client/{id}", "DELETE /client/{id}",
                "POST /account", "GET /account/{id}", "GET /account/number/{number}",
                "GET /account/{id}/statement", "DELETE /account/{id}",
                "POST /transaction", "GET /transaction", "GET /transaction/{id}");
        List<String> documented = new ArrayList<>();
        paths.fields().forEachRemaining(path -> path.getValue().fieldNames().forEachRemaining(method -> {
            if (HTTP_METHODS.contains(method)) {
                documented.add(method.toUpperCase() + " " + path.getKey());
            }
        }));

        assertTrue(documented.containsAll(expected), "faltam endpoints na documentação: " + missing(expected, documented));
        assertEquals(expected.size(), documented.size(), "há endpoints documentados que não estão na lista: " + documented);
    }

    @Test
    void docs_everyOperationShouldHaveSummaryAndResponses() throws Exception {
        apiDocs().get("paths").fields().forEachRemaining(path -> path.getValue().fields().forEachRemaining(op -> {
            if (!HTTP_METHODS.contains(op.getKey())) {
                return;
            }
            String name = op.getKey().toUpperCase() + " " + path.getKey();
            assertFalse(op.getValue().path("summary").asText().isBlank(), name + " sem summary");
            JsonNode responses = op.getValue().path("responses");
            // O springdoc descarta a resposta de sucesso automática quando há @ApiResponse; ela precisa ser explícita
            String success = firstSuccessCode(responses);
            assertFalse(success.isEmpty(), name + " sem resposta de sucesso (2xx)");
            if (!"DELETE".equals(op.getKey().toUpperCase())) {
                assertTrue(responses.path(success).has("content"), name + " sem corpo documentado na resposta " + success);
            }
        }));
    }

    @Test
    void docs_shouldDeclareBearerSecurityAndKeepAuthEndpointsOpen() throws Exception {
        JsonNode docs = apiDocs();

        JsonNode scheme = docs.at("/components/securitySchemes/bearerAuth");
        assertEquals("http", scheme.path("type").asText());
        assertEquals("bearer", scheme.path("scheme").asText());
        assertEquals("JWT", scheme.path("bearerFormat").asText());

        assertEquals("bearerAuth", docs.at("/security/0").fieldNames().next(), "segurança global ausente");
        assertEquals(0, docs.at("/paths/~1auth~1login/post/security").size(), "login não pode exigir token");
        assertEquals(0, docs.at("/paths/~1auth~1register/post/security").size(), "cadastro não pode exigir token");
        assertTrue(docs.at("/paths/~1client/get/security").isMissingNode(), "demais endpoints herdam a segurança global");
    }

    @Test
    void docs_shouldDocumentErrorsInTheApiFormatAndStatusCodes() throws Exception {
        JsonNode docs = apiDocs();

        JsonNode errorSchema = docs.at("/components/schemas/ErrorResponse/properties");
        assertTrue(errorSchema.has("status") && errorSchema.has("message") && errorSchema.has("timestamp"));

        JsonNode adminOnly = docs.at("/paths/~1client~1{id}/delete/responses");
        assertTrue(adminOnly.has("403") && adminOnly.has("404") && adminOnly.has("409") && adminOnly.has("401"));
        assertTrue(docs.at("/paths/~1transaction/post/responses").has("409"));
        assertFalse(docs.at("/paths/~1auth~1login/post/responses").has("403"));
        assertEquals("#/components/responses/Unauthorized",
                docs.at("/paths/~1client/get/responses/401/$ref").asText());
    }

    @Test
    void docs_statementShouldExposePagedResponseAndHideTheSpringPageableObject() throws Exception {
        JsonNode docs = apiDocs();
        JsonNode operation = docs.at("/paths/~1account~1{id}~1statement/get");

        List<String> parameters = new ArrayList<>();
        operation.path("parameters").forEach(parameter -> parameters.add(parameter.path("name").asText()));
        assertTrue(parameters.containsAll(List.of("id", "from", "to", "page", "size")), parameters.toString());
        assertFalse(parameters.contains("pageable"), "o objeto Pageable do Spring não deve aparecer: " + parameters);

        String schemaRef = operation.at("/responses/200/content/*~1*/schema/$ref").asText();
        if (schemaRef.isEmpty()) {
            schemaRef = operation.at("/responses/200/content/application~1json/schema/$ref").asText();
        }
        String schemaName = schemaRef.substring(schemaRef.lastIndexOf('/') + 1);
        JsonNode schema = docs.at("/components/schemas/" + schemaName + "/properties");
        assertTrue(schema.has("content") && schema.has("page"), "esperado { content, page }: " + schema);
        assertTrue(docs.at("/components/schemas/" + schemaName + "/properties/page/$ref").asText().contains("PageMetadata"));
    }

    private static String firstSuccessCode(JsonNode responses) {
        Iterator<String> codes = responses.fieldNames();
        while (codes.hasNext()) {
            String code = codes.next();
            if (code.startsWith("2")) {
                return code;
            }
        }
        return "";
    }

    private JsonNode apiDocs() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private static List<String> missing(List<String> expected, List<String> documented) {
        List<String> result = new ArrayList<>(expected);
        result.removeAll(documented);
        return result;
    }
}
