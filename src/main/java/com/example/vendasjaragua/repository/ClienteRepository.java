package com.example.vendasjaragua.repository;

import com.example.vendasjaragua.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Buscar por filial
    Page<Cliente> findByFilial(String filial, Pageable pageable);

    // Buscar por filial e período
    Page<Cliente> findByFilialAndDataBetween(String filial, LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Buscar com filtro de pesquisa e filial
    @Query("SELECT c FROM Cliente c WHERE " +
           "c.filial = :filial AND " +
           "(COALESCE(:startDate, NULL) IS NULL OR c.data >= :startDate) AND " +
           "(COALESCE(:endDate, NULL) IS NULL OR c.data <= :endDate) AND " +
           "(LOWER(c.nome) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(c.vendedor) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(c.cpfCnpj) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(c.telefone) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Cliente> searchClientesByFilial(
            @Param("filial") String filial,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("search") String search,
            Pageable pageable);

    // Buscar todas as filiais distintas
    @Query("SELECT DISTINCT c.filial FROM Cliente c ORDER BY c.filial")
    List<String> findDistinctFiliais();

    // Buscar vendedores únicos por filial
    @Query("SELECT DISTINCT c.vendedor FROM Cliente c WHERE c.filial = :filial AND c.vendedor IS NOT NULL ORDER BY c.vendedor")
    List<String> findVendedoresByFilial(@Param("filial") String filial);

    // Estatísticas por filial
    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.filial = :filial AND " +
           "(COALESCE(:startDate, NULL) IS NULL OR c.data >= :startDate) AND " +
           "(COALESCE(:endDate, NULL) IS NULL OR c.data <= :endDate)")
    Long countByFilialAndDataBetween(
            @Param("filial") String filial,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(c.valorDebito) FROM Cliente c WHERE c.filial = :filial AND " +
           "(COALESCE(:startDate, NULL) IS NULL OR c.data >= :startDate) AND " +
           "(COALESCE(:endDate, NULL) IS NULL OR c.data <= :endDate)")
    Double sumValorDebitoByFilialAndDataBetween(
            @Param("filial") String filial,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(c.valorPago) FROM Cliente c WHERE c.filial = :filial AND " +
           "(COALESCE(:startDate, NULL) IS NULL OR c.data >= :startDate) AND " +
           "(COALESCE(:endDate, NULL) IS NULL OR c.data <= :endDate)")
    Double sumValorPagoByFilialAndDataBetween(
            @Param("filial") String filial,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Dashboard - vendas por mês e filial
    @Query("SELECT function('to_char', c.data, 'YYYY-MM') as mes, " +
           "c.filial, " +
           "SUM(c.valorDebito) as total, " +
           "COUNT(c) as quantidade " +
           "FROM Cliente c WHERE " +
           "c.data BETWEEN :inicio AND :fim AND " +
           "(COALESCE(:filiais, NULL) IS NULL OR c.filial IN :filiais) AND " +
           "(COALESCE(:vendedores, NULL) IS NULL OR c.vendedor IN :vendedores) " +
           "GROUP BY function('to_char', c.data, 'YYYY-MM'), c.filial " +
           "ORDER BY mes")
    List<Object[]> findVendasPorMesEFilial(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("filiais") List<String> filiais,
            @Param("vendedores") List<String> vendedores);

    // Dashboard - vendas por vendedor e filial
    @Query("SELECT c.vendedor, c.filial, " +
           "SUM(c.valorDebito) as total, " +
           "SUM(c.valorPago) as pago, " +
           "COUNT(c) as quantidade " +
           "FROM Cliente c WHERE " +
           "c.data BETWEEN :inicio AND :fim AND " +
           "(COALESCE(:filiais, NULL) IS NULL OR c.filial IN :filiais) AND " +
           "(COALESCE(:vendedores, NULL) IS NULL OR c.vendedor IN :vendedores) " +
           "GROUP BY c.vendedor, c.filial " +
           "ORDER BY total DESC")
    List<Object[]> findVendasPorVendedorEFilial(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("filiais") List<String> filiais,
            @Param("vendedores") List<String> vendedores);
}
