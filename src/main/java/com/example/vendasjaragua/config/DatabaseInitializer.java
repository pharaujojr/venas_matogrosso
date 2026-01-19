package com.example.vendasjaragua.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        System.out.println("=== Verificando vendas sem produtos especificados ===");
        
        String checkSql = "SELECT COUNT(*) FROM vendas_jaragua WHERE produto IS NULL OR produto = '[]'::jsonb";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
        
        if (count != null && count > 0) {
            System.out.println("Encontradas " + count + " vendas sem produtos. Corrigindo...");
            
            String updateSql = """
                UPDATE vendas_jaragua 
                SET produto = jsonb_build_array(
                    jsonb_build_object(
                        'nomeProduto', 'Não Especificado',
                        'grupo', '',
                        'quantidade', 1,
                        'valorUnitarioVenda', valor_venda,
                        'valorUnitarioCusto', 0
                    )
                )
                WHERE produto IS NULL OR produto = '[]'::jsonb
                """;
            
            int updated = jdbcTemplate.update(updateSql);
            System.out.println("✓ " + updated + " vendas atualizadas com produto 'Não Especificado'");
        } else {
            System.out.println("✓ Todas as vendas já possuem produtos especificados");
        }
    }
}
