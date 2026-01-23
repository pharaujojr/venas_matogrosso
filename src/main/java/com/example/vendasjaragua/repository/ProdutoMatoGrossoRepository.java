package com.example.vendasjaragua.repository;

import com.example.vendasjaragua.model.ProdutoMatoGrosso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoMatoGrossoRepository extends JpaRepository<ProdutoMatoGrosso, Long> {

    // Buscar por descrição
    List<ProdutoMatoGrosso> findByDescricaoContainingIgnoreCase(String descricao);

    // Buscar por grupo
    List<ProdutoMatoGrosso> findByGrupo(String grupo);

    // Buscar todos os grupos distintos
    @Query("SELECT DISTINCT p.grupo FROM ProdutoMatoGrosso p WHERE p.grupo IS NOT NULL ORDER BY p.grupo")
    List<String> findDistinctGrupos();

    // Buscar produto por descrição exata
    @Query("SELECT p FROM ProdutoMatoGrosso p WHERE LOWER(p.descricao) = LOWER(:descricao)")
    ProdutoMatoGrosso findByDescricaoExata(@Param("descricao") String descricao);
}
