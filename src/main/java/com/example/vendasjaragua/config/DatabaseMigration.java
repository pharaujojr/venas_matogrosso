package com.example.vendasjaragua.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Verificando migração da coluna 'produto' para JSONB...");
        try {
            // Check if column is already jsonb
            String columnType = jdbcTemplate.queryForObject(
                "SELECT data_type FROM information_schema.columns WHERE table_name = 'vendas_jaragua' AND column_name = 'produto'",
                String.class
            );

            if ("character varying".equalsIgnoreCase(columnType) || "text".equalsIgnoreCase(columnType)) {
                System.out.println("Coluna 'produto' é do tipo texto. Iniciando conversão para JSONB...");
                String migrationSql = "ALTER TABLE vendas_jaragua ALTER COLUMN produto TYPE jsonb USING " +
                                      "CASE WHEN produto IS NULL OR trim(produto) = '' THEN '[]'::jsonb " +
                                      "ELSE jsonb_build_array(jsonb_build_object(" +
                                      "'produtoId', null, " +
                                      "'nomeProduto', produto, " +
                                      "'quantidade', 1, " +
                                      "'valorUnitarioVenda', 0, " +
                                      "'valorUnitarioCusto', 0" +
                                      ")) END";
                jdbcTemplate.execute(migrationSql);
                System.out.println("Migração concluída com sucesso!");
            } else {
                System.out.println("Coluna 'produto' já está no formato correto (" + columnType + "). Nenhuma ação necessária.");
            }
        } catch (Exception e) {
            System.err.println("Erro durante verificação/migração do banco de dados: " + e.getMessage());
        }
    }
}
