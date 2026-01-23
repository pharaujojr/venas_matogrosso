package com.example.vendasjaragua.repository;

import com.example.vendasjaragua.model.Filial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilialRepository extends JpaRepository<Filial, Long> {
    
    List<Filial> findByAtivoTrue();
    
    Optional<Filial> findByNome(String nome);
    
    Optional<Filial> findByCodigo(String codigo);
}
