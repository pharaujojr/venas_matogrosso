package com.example.vendasjaragua.repository;

import com.example.vendasjaragua.model.VendedorMatoGrosso;
import com.example.vendasjaragua.model.Filial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendedorMatoGrossoRepository extends JpaRepository<VendedorMatoGrosso, Long> {

    // Buscar vendedores por filial
    List<VendedorMatoGrosso> findByFilial(Filial filial);

    // Buscar vendedores ativos por filial
    List<VendedorMatoGrosso> findByFilialAndAtivoTrue(Filial filial);

    // Buscar vendedores ativos
    List<VendedorMatoGrosso> findByAtivoTrue();

    // Buscar vendedor por nome e filial
    @Query("SELECT v FROM VendedorMatoGrosso v WHERE LOWER(v.nome) = LOWER(:nome) AND v.filial = :filial")
    VendedorMatoGrosso findByNomeAndFilial(@Param("nome") String nome, @Param("filial") Filial filial);

    // Buscar todas as filiais distintas
    @Query("SELECT DISTINCT v.filial.nome FROM VendedorMatoGrosso v ORDER BY v.filial.nome")
    List<String> findDistinctFiliais();
}
