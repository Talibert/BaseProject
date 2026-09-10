package com.example.api_docker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class OpenApiIntegrationTest extends IntegrationAbstractTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar a documentação OpenAPI sem necessidade de autenticação")
    void shouldReturnOpenApiDocumentationWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title", containsString("Base Project API")))
                .andExpect(jsonPath("$.paths['/auth/login']").exists())
                .andExpect(jsonPath("$.paths['/user/register']").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists());
    }

    @Test
    @DisplayName("Deve carregar o Swagger UI HTML com sucesso")
    void shouldLoadSwaggerUiSuccessfully() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar a documentação do grupo de negócio sem rotas do Actuator")
    void shouldReturnBusinessGroupDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs/1-aplicacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/auth/login']").exists())
                .andExpect(jsonPath("$.paths['/user/register']").exists())
                .andExpect(jsonPath("$.paths['/actuator/health']").doesNotExist());
    }

    @Test
    @DisplayName("Deve retornar a documentação do grupo do Actuator contendo as rotas de monitoramento")
    void shouldReturnActuatorGroupDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs/2-actuator"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/actuator/health']").exists())
                .andExpect(jsonPath("$.paths['/auth/login']").doesNotExist());
    }
}
