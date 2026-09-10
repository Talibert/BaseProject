package com.example.api_docker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração para os endpoints de monitoramento e saúde do Spring Boot Actuator.
 *
 * <p><b>Contexto e Infraestrutura do Teste:</b><br>
 * Este teste estende {@link IntegrationAbstractTests}, o que significa que o contexto completo
 * do Spring Boot é inicializado de verdade (sem mocks para a infraestrutura básica):
 * <ul>
 *   <li>Sobe um banco de dados real em memória (H2) com migrações do Flyway executadas.</li>
 *   <li>Sobe um broker Kafka real em memória (EmbeddedKafka).</li>
 *   <li>O Actuator executa verificações reais de conectividade contra esses componentes (DataSource, Disco, etc.).</li>
 * </ul>
 *
 * <p><b>Conceitos das Probes (Kubernetes / Docker):</b><br>
 * <ul>
 *   <li><b>Liveness Probe ({@code /actuator/health/liveness}):</b><br>
 *       Verifica se a aplicação está <i>viva</i> internamente e a JVM não congelou em deadlock ou travamento crítico.
 *       Se retornar {@code DOWN}, o orquestrador (Docker/K8s) <b>reinicia o container</b>.</li>
 *
 *   <li><b>Readiness Probe ({@code /actuator/health/readiness}):</b><br>
 *       Verifica se a aplicação está <i>pronta</i> para receber requisições de clientes (boot concluído, migrations
 *       finalizadas, conexões estabelecidas). Se retornar {@code DOWN}, o Load Balancer <b>pausa o envio de tráfego</b>
 *       para este container até que ele se recupere, sem reiniciá-lo.</li>
 *
 *   <li><b>Health Global ({@code /actuator/health}):</b><br>
 *       Agrega o estado geral da aplicação e a saúde detalhada de cada componente (DB, Kafka, disco),
 *       utilizado por dashboards e sistemas de observabilidade externos (Prometheus, Datadog, Grafana).</li>
 * </ul>
 */
@AutoConfigureMockMvc
class ActuatorHealthIntegrationTest extends IntegrationAbstractTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar status UP no endpoint /actuator/health sem autenticação")
    void shouldReturnHealthUpWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Deve retornar liveness probe com status UP")
    void shouldReturnLivenessProbeUp() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Deve retornar readiness probe com status UP")
    void shouldReturnReadinessProbeUp() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
