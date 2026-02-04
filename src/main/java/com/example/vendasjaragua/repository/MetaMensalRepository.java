package com.example.vendasjaragua.repository;

import com.example.vendasjaragua.model.MetaMensal;
import com.example.vendasjaragua.model.Filial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetaMensalRepository extends JpaRepository<MetaMensal, Long> {
    
    Optional<MetaMensal> findByFilialAndMesAndAno(Filial filial, Integer mes, Integer ano);
    
    List<MetaMensal> findByMesAndAno(Integer mes, Integer ano);
    
    @Query("SELECT m FROM MetaMensal m WHERE m.mes = :mes AND m.ano = :ano")
    List<MetaMensal> findAllByMesAndAno(@Param("mes") Integer mes, @Param("ano") Integer ano);
}
