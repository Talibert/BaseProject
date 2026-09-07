package com.example.api_docker.infra.tools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DBInstallTest {

    @Test
    @DisplayName("Deve gerar comandos DDL contendo CREATE TABLE para as entidades mapeadas")
    void shouldGenerateFormattedDDLSuccessfully() throws Exception {
        List<String> ddlStatements = DBInstall.generateFormattedDDL();

        assertThat(ddlStatements).isNotEmpty();

        // Verifica que a tabela users é gerada com suas colunas principais
        String fullDDL = String.join("\n", ddlStatements);
        assertThat(fullDDL)
                .containsIgnoringCase("create table users")
                .containsIgnoringCase("user_id")
                .containsIgnoringCase("email")
                .containsIgnoringCase("first_name")
                .containsIgnoringCase("last_name")
                .containsIgnoringCase("password_hash")
                .containsIgnoringCase("primary key (user_id)");
    }

    @Test
    @DisplayName("Deve executar o método main() sem lançar exceções")
    void shouldExecuteMainWithoutThrowing() {
        assertDoesNotThrow(() -> DBInstall.main(new String[0]));
    }
}
