package com.example.vendasjaragua.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class VendaRepositoryImpl implements VendaRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public List<Map<String, Object>> findFaturamentoPorGrupoDynamic(
            LocalDate dataInicio,
            LocalDate dataFim,
            List<String> times,
            List<String> vendedores,
            List<String> grupos,
            List<String> produtos) {
        
        StringBuilder sql = new StringBuilder();
        // Use TRIM and NULLIF to handle empty strings and whitespace being treated differently than NULL
        sql.append("SELECT COALESCE(NULLIF(TRIM(p.grupo), ''), 'Não Especificado') as grupo, ");
        sql.append("SUM(CAST(COALESCE(NULLIF(item->>'valorUnitarioVenda', ''), '0') AS NUMERIC) * ");
        sql.append("CAST(COALESCE(NULLIF(item->>'quantidade', ''), '0') AS INTEGER)) as total ");
        sql.append("FROM financeiro_clientes v ");
        sql.append("CROSS JOIN jsonb_array_elements(COALESCE(v.produto, CAST('[]' AS jsonb))) AS item ");
        sql.append("LEFT JOIN matogrosso_produtos p ON item->>'nomeProduto' = p.descricao ");
        sql.append("WHERE v.data BETWEEN :dataInicio AND :dataFim ");
        sql.append("AND v.ganho = true ");
        
        if (times != null && !times.isEmpty()) {
            sql.append("AND v.filial IN (:times) ");
        }
        if (vendedores != null && !vendedores.isEmpty()) {
            sql.append("AND v.vendedor IN (:vendedores) ");
        }
        if (grupos != null && !grupos.isEmpty()) {
            sql.append("AND (p.grupo IN (:grupos) OR ");
            sql.append("(COALESCE(p.grupo, '') = '' AND 'Não Especificado' IN (:grupos))) ");
        }
        if (produtos != null && !produtos.isEmpty()) {
            sql.append("AND item->>'nomeProduto' IN (:produtos) ");
        }
        
        sql.append("GROUP BY grupo ");
        sql.append("ORDER BY total DESC");
        
        var query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("dataInicio", dataInicio);
        query.setParameter("dataFim", dataFim);
        
        if (times != null && !times.isEmpty()) query.setParameter("times", times);
        if (vendedores != null && !vendedores.isEmpty()) query.setParameter("vendedores", vendedores);
        if (grupos != null && !grupos.isEmpty()) query.setParameter("grupos", grupos);
        if (produtos != null && !produtos.isEmpty()) query.setParameter("produtos", produtos);
        
        List<Object[]> results = query.getResultList();
        List<Map<String, Object>> mapped = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("grupo", row[0] == null ? "Não Especificado" : row[0]);
            map.put("total", row[1]);
            mapped.add(map);
        }
        return mapped;
    }

    @Override
    public List<Map<String, Object>> findVendasPorMes(
            LocalDate dataInicio,
            LocalDate dataFim,
            List<String> times,
            List<String> vendedores,
            List<String> grupos,
            List<String> produtos,
            Boolean closing,
            Boolean ganho) {
        
        StringBuilder sql = new StringBuilder();
        boolean filteringByProducts = (grupos != null && !grupos.isEmpty()) || (produtos != null && !produtos.isEmpty());

        sql.append("SELECT TO_CHAR(v.data, 'YYYY-MM') as mes, ");
        
        if (filteringByProducts) {
             sql.append("SUM(CAST(COALESCE(NULLIF(item->>'valorUnitarioVenda', ''), '0') AS NUMERIC) * ");
             sql.append("CAST(COALESCE(NULLIF(item->>'quantidade', ''), '0') AS INTEGER)) as total ");
        } else {
             sql.append("SUM(COALESCE(v.valor_debito, 0)) as total ");
        }
        
        sql.append("FROM financeiro_clientes v ");
        
        if (filteringByProducts) {
            sql.append("CROSS JOIN jsonb_array_elements(COALESCE(v.produto, CAST('[]' AS jsonb))) AS item ");
            sql.append("LEFT JOIN matogrosso_produtos p ON item->>'nomeProduto' = p.descricao ");
        }
        
        sql.append("WHERE v.data BETWEEN :dataInicio AND :dataFim ");
        
        if (ganho != null) {
            sql.append("AND v.ganho = :ganho ");
        } else {
            sql.append("AND v.ganho = true ");
        }
        
        if (closing != null) {
            sql.append("AND v.closing = :closing ");
        }
        
        if (times != null && !times.isEmpty()) {
            sql.append("AND v.filial IN (:times) ");
        }
        if (vendedores != null && !vendedores.isEmpty()) {
            sql.append("AND v.vendedor IN (:vendedores) ");
        }
        
        if (grupos != null && !grupos.isEmpty()) {
            sql.append("AND (p.grupo IN (:grupos) OR ");
            sql.append("(COALESCE(p.grupo, '') = '' AND 'Não Especificado' IN (:grupos))) ");
        }
        if (produtos != null && !produtos.isEmpty()) {
            sql.append("AND item->>'nomeProduto' IN (:produtos) ");
        }
        
        sql.append("GROUP BY mes ");
        sql.append("ORDER BY mes");
        
        var query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("dataInicio", dataInicio);
        query.setParameter("dataFim", dataFim);
        
        if (ganho != null) query.setParameter("ganho", ganho);
        if (closing != null) query.setParameter("closing", closing);
        
        if (times != null && !times.isEmpty()) query.setParameter("times", times);
        if (vendedores != null && !vendedores.isEmpty()) query.setParameter("vendedores", vendedores);
        if (grupos != null && !grupos.isEmpty()) query.setParameter("grupos", grupos);
        if (produtos != null && !produtos.isEmpty()) query.setParameter("produtos", produtos);
        
        List<Object[]> results = query.getResultList();
        List<Map<String, Object>> mapped = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("mes", row[0]);
            map.put("total", row[1]);
            mapped.add(map);
        }
        return mapped;
    }

    @Override
    public long countVendasFiltradas(
            LocalDate dataInicio,
            LocalDate dataFim,
            List<String> times,
            List<String> vendedores,
            List<String> grupos,
            List<String> produtos,
            Boolean closing,
            Boolean ganho) {
        
        StringBuilder sql = new StringBuilder();
        boolean filteringByProducts = (grupos != null && !grupos.isEmpty()) || (produtos != null && !produtos.isEmpty());
        
        if (filteringByProducts) {
             sql.append("SELECT COUNT(DISTINCT v.id) FROM financeiro_clientes v ");
             sql.append("CROSS JOIN jsonb_array_elements(COALESCE(v.produto, CAST('[]' AS jsonb))) AS item ");
             sql.append("LEFT JOIN matogrosso_produtos p ON item->>'nomeProduto' = p.descricao ");
        } else {
             sql.append("SELECT COUNT(v.id) FROM financeiro_clientes v ");
        }
        
        sql.append("WHERE v.data BETWEEN :dataInicio AND :dataFim ");
        
        if (ganho != null) {
            sql.append("AND v.ganho = :ganho ");
        } else {
            sql.append("AND v.ganho = true ");
        }
        
        if (closing != null) {
            sql.append("AND v.closing = :closing ");
        }
        
        if (times != null && !times.isEmpty()) {
            sql.append("AND v.filial IN (:times) ");
        }
        if (vendedores != null && !vendedores.isEmpty()) {
            sql.append("AND v.vendedor IN (:vendedores) ");
        }

        if (grupos != null && !grupos.isEmpty()) {
            sql.append("AND (p.grupo IN (:grupos) OR ");
            sql.append("(COALESCE(p.grupo, '') = '' AND 'Não Especificado' IN (:grupos))) ");
        }
        if (produtos != null && !produtos.isEmpty()) {
            sql.append("AND item->>'nomeProduto' IN (:produtos) ");
        }
        
        var query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("dataInicio", dataInicio);
        query.setParameter("dataFim", dataFim);
        
        if (ganho != null) query.setParameter("ganho", ganho);
        if (closing != null) query.setParameter("closing", closing);
        
        if (times != null && !times.isEmpty()) query.setParameter("times", times);
        if (vendedores != null && !vendedores.isEmpty()) query.setParameter("vendedores", vendedores);
        if (grupos != null && !grupos.isEmpty()) query.setParameter("grupos", grupos);
        if (produtos != null && !produtos.isEmpty()) query.setParameter("produtos", produtos);
        
        return ((Number) query.getSingleResult()).longValue();
    }

    @Override
    public long countVendasComGanhoFiltradas(
            LocalDate dataInicio,
            LocalDate dataFim,
            List<String> times,
            List<String> vendedores,
            List<String> grupos,
            List<String> produtos,
            Boolean closing) {
        
        StringBuilder sql = new StringBuilder();
        boolean filteringByProducts = (grupos != null && !grupos.isEmpty()) || (produtos != null && !produtos.isEmpty());
        
        if (filteringByProducts) {
             sql.append("SELECT COUNT(DISTINCT v.id) FROM financeiro_clientes v ");
             sql.append("CROSS JOIN jsonb_array_elements(COALESCE(v.produto, CAST('[]' AS jsonb))) AS item ");
             sql.append("LEFT JOIN matogrosso_produtos p ON item->>'nomeProduto' = p.descricao ");
        } else {
             sql.append("SELECT COUNT(v.id) FROM financeiro_clientes v ");
        }

        sql.append("WHERE v.data BETWEEN :dataInicio AND :dataFim ");
        sql.append("AND v.ganho = true ");
        
        if (closing != null) {
            sql.append("AND v.closing = :closing ");
        }
        
        if (times != null && !times.isEmpty()) {
            sql.append("AND v.filial IN (:times) ");
        }
        if (vendedores != null && !vendedores.isEmpty()) {
            sql.append("AND v.vendedor IN (:vendedores) ");
        }
        
        if (grupos != null && !grupos.isEmpty()) {
            sql.append("AND (p.grupo IN (:grupos) OR ");
            sql.append("(COALESCE(p.grupo, '') = '' AND 'Não Especificado' IN (:grupos))) ");
        }
        if (produtos != null && !produtos.isEmpty()) {
            sql.append("AND item->>'nomeProduto' IN (:produtos) ");
        }
        
        var query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("dataInicio", dataInicio);
        query.setParameter("dataFim", dataFim);
        
        if (closing != null) query.setParameter("closing", closing);
        
        if (times != null && !times.isEmpty()) query.setParameter("times", times);
        if (vendedores != null && !vendedores.isEmpty()) query.setParameter("vendedores", vendedores);
        if (grupos != null && !grupos.isEmpty()) query.setParameter("grupos", grupos);
        if (produtos != null && !produtos.isEmpty()) query.setParameter("produtos", produtos);
        
        return ((Number) query.getSingleResult()).longValue();
    }

    @Override
    public List<Map<String, Object>> findVendasPorVendedorDynamic(
            LocalDate dataInicio,
            LocalDate dataFim,
            List<String> times,
            List<String> vendedores,
            List<String> grupos,
            List<String> produtos,
            Boolean closing,
            Boolean ganho) {
        
        StringBuilder sql = new StringBuilder();
        boolean filteringByProducts = (grupos != null && !grupos.isEmpty()) || (produtos != null && !produtos.isEmpty());

        sql.append("SELECT v.vendedor, ");
        
        if (filteringByProducts) {
             sql.append("SUM(CAST(COALESCE(NULLIF(item->>'valorUnitarioVenda', ''), '0') AS NUMERIC) * ");
             sql.append("CAST(COALESCE(NULLIF(item->>'quantidade', ''), '0') AS INTEGER)) as total ");
        } else {
             sql.append("SUM(COALESCE(v.valor_debito, 0)) as total ");
        }
        
        sql.append("FROM financeiro_clientes v ");
        
        if (filteringByProducts) {
            sql.append("CROSS JOIN jsonb_array_elements(COALESCE(v.produto, CAST('[]' AS jsonb))) AS item ");
            sql.append("LEFT JOIN matogrosso_produtos p ON item->>'nomeProduto' = p.descricao ");
        }
        
        sql.append("WHERE v.data BETWEEN :dataInicio AND :dataFim ");
        
        if (ganho != null) {
            sql.append("AND v.ganho = :ganho ");
        } else {
            sql.append("AND v.ganho = true ");
        }
        
        if (closing != null) {
            sql.append("AND v.closing = :closing ");
        }
        
        if (times != null && !times.isEmpty()) {
            sql.append("AND v.filial IN (:times) ");
        }
        if (vendedores != null && !vendedores.isEmpty()) {
            sql.append("AND v.vendedor IN (:vendedores) ");
        }
        
        if (grupos != null && !grupos.isEmpty()) {
            sql.append("AND (p.grupo IN (:grupos) OR ");
            sql.append("(COALESCE(p.grupo, '') = '' AND 'Não Especificado' IN (:grupos))) ");
        }
        if (produtos != null && !produtos.isEmpty()) {
            sql.append("AND item->>'nomeProduto' IN (:produtos) ");
        }
        
        sql.append("GROUP BY v.vendedor ");
        sql.append("ORDER BY total DESC");
        
        var query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("dataInicio", dataInicio);
        query.setParameter("dataFim", dataFim);
        
        if (ganho != null) query.setParameter("ganho", ganho);
        if (closing != null) query.setParameter("closing", closing);
        
        if (times != null && !times.isEmpty()) query.setParameter("times", times);
        if (vendedores != null && !vendedores.isEmpty()) query.setParameter("vendedores", vendedores);
        if (grupos != null && !grupos.isEmpty()) query.setParameter("grupos", grupos);
        if (produtos != null && !produtos.isEmpty()) query.setParameter("produtos", produtos);
        
        List<Object[]> results = query.getResultList();
        List<Map<String, Object>> mapped = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("vendedor", row[0]);
            map.put("total", row[1]);
            mapped.add(map);
        }
        return mapped;
    }

    @Override
    public List<Object[]> findVendasPorTimeDynamic(
            LocalDate dataInicio,
            LocalDate dataFim,
            List<String> times,
            List<String> vendedores,
            List<String> grupos,
            List<String> produtos,
            Boolean closing,
            Boolean ganho) {
        
        StringBuilder sql = new StringBuilder();
        boolean filteringByProducts = (grupos != null && !grupos.isEmpty()) || (produtos != null && !produtos.isEmpty());

        sql.append("SELECT v.filial, ");
        
        if (filteringByProducts) {
             sql.append("SUM(CAST(COALESCE(NULLIF(item->>'valorUnitarioVenda', ''), '0') AS NUMERIC) * ");
             sql.append("CAST(COALESCE(NULLIF(item->>'quantidade', ''), '0') AS INTEGER)) as total ");
        } else {
             sql.append("SUM(COALESCE(v.valor_debito, 0)) as total ");
        }
        
        sql.append("FROM financeiro_clientes v ");
        
        if (filteringByProducts) {
            sql.append("CROSS JOIN jsonb_array_elements(COALESCE(v.produto, CAST('[]' AS jsonb))) AS item ");
            sql.append("LEFT JOIN matogrosso_produtos p ON item->>'nomeProduto' = p.descricao ");
        }
        
        sql.append("WHERE v.data BETWEEN :dataInicio AND :dataFim ");
        
        if (ganho != null) {
            sql.append("AND v.ganho = :ganho ");
        } else {
            sql.append("AND v.ganho = true ");
        }
        
        if (closing != null) {
            sql.append("AND v.closing = :closing ");
        }
        
        if (times != null && !times.isEmpty()) {
            sql.append("AND v.filial IN (:times) ");
        }
        if (vendedores != null && !vendedores.isEmpty()) {
            sql.append("AND v.vendedor IN (:vendedores) ");
        }

        if (grupos != null && !grupos.isEmpty()) {
            sql.append("AND (p.grupo IN (:grupos) OR ");
            sql.append("(COALESCE(p.grupo, '') = '' AND 'Não Especificado' IN (:grupos))) ");
        }
        if (produtos != null && !produtos.isEmpty()) {
            sql.append("AND item->>'nomeProduto' IN (:produtos) ");
        }
        
        sql.append("GROUP BY v.filial ");
        sql.append("ORDER BY total DESC");
        
        var query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("dataInicio", dataInicio);
        query.setParameter("dataFim", dataFim);
        
        if (ganho != null) query.setParameter("ganho", ganho);
        if (closing != null) query.setParameter("closing", closing);
        
        if (times != null && !times.isEmpty()) query.setParameter("times", times);
        if (vendedores != null && !vendedores.isEmpty()) query.setParameter("vendedores", vendedores);
        if (grupos != null && !grupos.isEmpty()) query.setParameter("grupos", grupos);
        if (produtos != null && !produtos.isEmpty()) query.setParameter("produtos", produtos);
        
        return query.getResultList();
    }

    @Override
    public List<com.example.vendasjaragua.model.Venda> findVendasDetalhadasDynamic(
            LocalDate dataInicio,
            LocalDate dataFim,
            List<String> times,
            List<String> vendedores,
            List<String> grupos,
            List<String> produtos,
            Boolean closing,
            Boolean ganho) {
        
        StringBuilder sql = new StringBuilder();
        boolean filteringByProducts = (grupos != null && !grupos.isEmpty()) || (produtos != null && !produtos.isEmpty());
        
        if (filteringByProducts) {
             sql.append("SELECT DISTINCT v.* FROM financeiro_clientes v ");
             sql.append("CROSS JOIN jsonb_array_elements(COALESCE(v.produto, CAST('[]' AS jsonb))) AS item ");
             sql.append("LEFT JOIN matogrosso_produtos p ON item->>'nomeProduto' = p.descricao ");
        } else {
             sql.append("SELECT v.* FROM financeiro_clientes v ");
        }
        
        sql.append("WHERE v.data BETWEEN :dataInicio AND :dataFim ");
        
        if (ganho != null) {
             sql.append("AND v.ganho = :ganho ");
        } else {
             // Default behavior if null is mimicking other methods, supposedly 'true' if null per findVendasPorMes? 
             // Actually findVendasPorTimeDynamic uses if (ganho != null) append... 
             // But wait, findVendasPorMes in my last edit has: 
             // if (ganho != null) ... else ... = true
             // findVendasPorTimeDynamic in my last edit has:
             // if (ganho != null) ... (no else)
             // Let's check consistency.
             // In findVendasPorTimeDynamic (last visible version above):
             // if (ganho != null) { sql.append("AND v.ganho = :ganho "); } 
             // (NO ELSE)
             // This means if 'ganho' is null, it returns ALL (true and false)?
             // The user mentioned "divergencia" (divergence).
             // If Total uses "ganho=true" implicitly and "VendasPorFilial" uses "All", that's a problem.
             // But stats endpoint passes ganho.
             
             // Let's mimic findVendasPorTimeDynamic logic EXACTLY as it is naturally.
             // Wait, I should check if findVendasPorTimeDynamic logic IS correct.
             // In findVendasPorMes: else { append("AND v.ganho = true ") }
             // In findVendasPorTimeDynamic: I see no else block in the code I just wrote. 
             // Let me check my previous tool output. 
        } 
        
        // Checking previous read/edit:
        // findVendasPorTimeDynamic:
        // if (ganho != null) { sql.append("AND v.ganho = :ganho "); } 
        
        // This means it includes FALSE (losses/pending) if ganho is null.
        // Whereas findVendasPorMes defaults to TRUE.
        
        // This inconsistency might be the cause of divergence too!
        // But the user asked for a button to debug "this chart" (the pie chart).
        // The pie chart usually calls /times endpoint. 
        // I should verify what the controller passes.
        
        if (ganho != null) {
            sql.append("AND v.ganho = :ganho ");
        } else {
            sql.append("AND v.ganho = true ");
        }
        
        if (closing != null) {
            sql.append("AND v.closing = :closing ");
        }
        
        if (times != null && !times.isEmpty()) {
            sql.append("AND v.filial IN (:times) ");
        }
        if (vendedores != null && !vendedores.isEmpty()) {
            sql.append("AND v.vendedor IN (:vendedores) ");
        }
        
        if (grupos != null && !grupos.isEmpty()) {
            sql.append("AND (p.grupo IN (:grupos) OR ");
            sql.append("(COALESCE(p.grupo, '') = '' AND 'Não Especificado' IN (:grupos))) ");
        }
        if (produtos != null && !produtos.isEmpty()) {
            sql.append("AND item->>'nomeProduto' IN (:produtos) ");
        }
        
        sql.append("ORDER BY v.data DESC");
        
        var query = entityManager.createNativeQuery(sql.toString(), com.example.vendasjaragua.model.Venda.class);
        query.setParameter("dataInicio", dataInicio);
        query.setParameter("dataFim", dataFim);
        
        if (ganho != null) query.setParameter("ganho", ganho);
        if (closing != null) query.setParameter("closing", closing);
        
        if (times != null && !times.isEmpty()) query.setParameter("times", times);
        if (vendedores != null && !vendedores.isEmpty()) query.setParameter("vendedores", vendedores);
        if (grupos != null && !grupos.isEmpty()) query.setParameter("grupos", grupos);
        if (produtos != null && !produtos.isEmpty()) query.setParameter("produtos", produtos);
        
        return query.getResultList();
    }
}
