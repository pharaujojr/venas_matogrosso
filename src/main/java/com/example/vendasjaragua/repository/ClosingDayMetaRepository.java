package com.example.vendasjaragua.repository;

import com.example.vendasjaragua.model.ClosingDayMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClosingDayMetaRepository extends JpaRepository<ClosingDayMeta, Long> {
    Optional<ClosingDayMeta> findByFilialNome(String filialNome);
}
