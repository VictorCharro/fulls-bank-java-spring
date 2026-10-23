package com.fulls_bank.Fulls_Bank.security;

import com.fulls_bank.Fulls_Bank.exceptions.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Documentação OpenAPI (springdoc): metadados, esquema de segurança Bearer JWT e respostas de erro reutilizáveis.
 * A interface fica em {@code /swagger-ui.html} e o JSON em {@code /v3/api-docs}.
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        Schema<?> errorSchema = ModelConverters.getInstance().read(ErrorResponse.class).get("ErrorResponse");
        Content errorContent = new Content().addMediaType("application/json", new MediaType()
                .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")));

        return new OpenAPI()
                .info(new Info()
                        .title("Fulls Bank API")
                        .version("0.0.1")
                        .description("API REST de um sistema bancário simplificado: clientes, contas, transações "
                                + "(depósito, saque e transferência) e extrato. Autenticação por JWT: faça login em "
                                + "`POST /auth/login` e envie o token no cabeçalho `Authorization: Bearer <token>` "
                                + "(botão **Authorize**)."))
                .components(new Components()
                        .addSchemas("ErrorResponse", errorSchema)
                        .addSecuritySchemes(BEARER, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        .addResponses("BadRequest", error("Requisição inválida: validação, regra de negócio ou formato", errorContent))
                        .addResponses("Unauthorized", error("Token ausente, inválido ou expirado (ou credenciais incorretas no login)", errorContent))
                        .addResponses("Forbidden", error("O papel do usuário não permite esta operação (somente ADMIN)", errorContent))
                        .addResponses("NotFound", error("Recurso não encontrado", errorContent))
                        .addResponses("Conflict", error("Conflito: recurso em uso ou alterado por outra operação ao mesmo tempo; pode repetir", errorContent))
                        .addResponses("ServiceUnavailable", error("Serviço temporariamente indisponível", errorContent)))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }

    /** Todo endpoint protegido pode responder 401; os que não exigem token (login e cadastro) ficam de fora. */
    @Bean
    public OperationCustomizer unauthorizedResponseCustomizer() {
        return (operation, handlerMethod) -> {
            boolean isPublic = operation.getSecurity() != null && operation.getSecurity().isEmpty();
            if (!isPublic && operation.getResponses() != null && !operation.getResponses().containsKey("401")) {
                operation.getResponses().addApiResponse("401", new ApiResponse().$ref("#/components/responses/Unauthorized"));
            }
            return operation;
        };
    }

    private static ApiResponse error(String description, Content content) {
        return new ApiResponse().description(description).content(content);
    }
}
