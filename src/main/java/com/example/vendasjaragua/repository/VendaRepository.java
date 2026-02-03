package com.example.vendasjaragua.repository;

import com.example.vendasjaragua.model.Venda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long>, VendaRepositoryCustom {

    @Query("SELECT v.vendedor, SUM(v.valorVenda) FROM Venda v WHERE v.ganho = true AND v.data BETWEEN :inicio AND :fim AND ((:filiais) IS NULL OR v.time IN (:filiais)) AND ((:vendedores) IS NULL OR v.vendedor IN (:vendedores)) GROUP BY v.vendedor ORDER BY SUM(v.valorVenda) DESC")
    List<Object[]> findVendasPorVendedor(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim, @Param("filiais") List<String> filiais, @Param("vendedores") List<String> vendedores);


    @Query("SELECT v.time, SUM(v.valorVenda) FROM Venda v WHERE v.data BETWEEN :inicio AND :fim " +
           "AND (:ganho IS NULL OR v.ganho = :ganho) " +
           "AND (:closing IS NULL OR v.closing = :closing) " +
           "GROUP BY v.time ORDER BY SUM(v.valorVenda) DESC")
    List<Object[]> findVendasPorTime(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim, 
                                      @Param("closing") Boolean closing, @Param("ganho") Boolean ganho);

    @Query("SELECT v.vendedor, SUM(v.valorVenda) FROM Venda v WHERE v.ganho = true AND v.time = :filial AND v.data BETWEEN :inicio AND :fim GROUP BY v.vendedor ORDER BY SUM(v.valorVenda) DESC")
    List<Object[]> findVendasPorVendedorAndTime(@Param("filial") String filial, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT v.vendedor, SUM(v.valorVenda) FROM Venda v WHERE v.ganho = true AND v.time IS NULL AND v.data BETWEEN :inicio AND :fim GROUP BY v.vendedor ORDER BY SUM(v.valorVenda) DESC")
    List<Object[]> findVendasPorVendedorWhereTimeIsNull(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT function('to_char', v.data, 'YYYY-MM') as mes, v.time, SUM(v.valorVenda) FROM Venda v WHERE v.ganho = true AND v.data BETWEEN :inicio AND :fim GROUP BY function('to_char', v.data, 'YYYY-MM'), v.time ORDER BY mes")
    List<Object[]> findEvolucaoVendasPorTime(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT DISTINCT v.vendedor FROM Venda v WHERE v.vendedor IS NOT NULL ORDER BY v.vendedor")
    List<String> findVendedoresUnicos();

    @Modifying
    @Transactional
    @Query("DELETE FROM Venda v WHERE v.cliente IS NULL AND v.vendedor IS NULL AND v.data IS NULL AND v.valorVenda IS NULL")
    void deleteEmptyRows();

    @Query("SELECT v FROM Venda v WHERE v.data BETWEEN :startDate AND :endDate")
    Page<Venda> findByDataBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    @Query("SELECT v FROM Venda v WHERE " +
           "v.ganho = true AND " +
           "(COALESCE(:startDate, NULL) IS NULL OR v.data >= :startDate) AND " +
           "(COALESCE(:endDate, NULL) IS NULL OR v.data <= :endDate) AND " +
           "(LOWER(v.cliente) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.vendedor) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.nf) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.ov) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.telefone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.cidade) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Venda> searchVendas(@Param("startDate") LocalDate startDate, 
                             @Param("endDate") LocalDate endDate, 
                             @Param("search") String search, 
                             Pageable pageable);

    @Query("SELECT v FROM Venda v WHERE " +
           "v.ganho = true AND " +
           "v.time = :filial AND " +
           "(COALESCE(:startDate, NULL) IS NULL OR v.data >= :startDate) AND " +
           "(COALESCE(:endDate, NULL) IS NULL OR v.data <= :endDate) AND " +
           "(LOWER(v.cliente) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.vendedor) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.nf) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.ov) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.telefone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.cidade) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Venda> searchVendasWithFilial(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("search") String search,
                                        @Param("filial") String filial,
                                        Pageable pageable);

    @Query("SELECT v FROM Venda v WHERE " +
           "v.time = :filial AND " +
           "(COALESCE(:startDate, NULL) IS NULL OR v.data >= :startDate) AND " +
           "(COALESCE(:endDate, NULL) IS NULL OR v.data <= :endDate)")
    Page<Venda> findByTimeAndDateRange(@Param("filial") String filial,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        Pageable pageable);

    @Query("SELECT v FROM Venda v WHERE " +
           "(COALESCE(:ganho, NULL) IS NULL OR v.ganho = :ganho) AND " +
           "(COALESCE(:startDate, NULL) IS NULL OR v.data >= :startDate) AND " +
           "(COALESCE(:endDate, NULL) IS NULL OR v.data <= :endDate) AND " +
           "(LOWER(v.cliente) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.vendedor) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.nf) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.ov) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.telefone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.cidade) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Venda> searchVendasWithGanho(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate,
                                       @Param("search") String search,
                                       @Param("ganho") Boolean ganho,
                                       Pageable pageable);

    @Query("SELECT v FROM Venda v WHERE " +
           "v.time = :filial AND " +
           "(COALESCE(:ganho, NULL) IS NULL OR v.ganho = :ganho) AND " +
           "(COALESCE(:startDate, NULL) IS NULL OR v.data >= :startDate) AND " +
           "(COALESCE(:endDate, NULL) IS NULL OR v.data <= :endDate) AND " +
           "(LOWER(v.cliente) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.vendedor) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.nf) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.ov) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.telefone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.cidade) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(v.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Venda> searchVendasWithFilialAndGanho(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate,
                                                @Param("search") String search,
                                                @Param("filial") String filial,
                                                @Param("ganho") Boolean ganho,
                                                Pageable pageable);

    @Query("SELECT v FROM Venda v WHERE " +
           "v.time = :filial AND " +
           "(COALESCE(:ganho, NULL) IS NULL OR v.ganho = :ganho) AND " +
           "(COALESCE(:startDate, NULL) IS NULL OR v.data >= :startDate) AND " +
           "(COALESCE(:endDate, NULL) IS NULL OR v.data <= :endDate)")
    Page<Venda> findByTimeAndDateRangeAndGanho(@Param("filial") String filial,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate,
                                                @Param("ganho") Boolean ganho,
                                                Pageable pageable);

    @Query("SELECT v FROM Venda v WHERE " +
           "(COALESCE(:ganho, NULL) IS NULL OR v.ganho = :ganho) AND " +
           "(COALESCE(:startDate, NULL) IS NULL OR v.data >= :startDate) AND " +
           "(COALESCE(:endDate, NULL) IS NULL OR v.data <= :endDate)")
    Page<Venda> findByDataBetweenAndGanho(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate,
                                           @Param("ganho") Boolean ganho,
                                           Pageable pageable);

    @Query("SELECT v FROM Venda v WHERE v.ganho = :ganho")
    Page<Venda> findByGanho(@Param("ganho") Boolean ganho, Pageable pageable);

    @Query("SELECT v FROM Venda v")
    Page<Venda> findAllVendas(Pageable pageable);
    
    // Métodos para relatório andressa
    List<Venda> findByDataBetween(LocalDate startDate, LocalDate endDate);
    
    List<Venda> findByDataBetweenAndVendedorObjIdIn(LocalDate startDate, LocalDate endDate, List<Long> vendedorIds);
    
    List<Venda> findByDataBetweenAndFilialIdIn(LocalDate startDate, LocalDate endDate, List<Long> filialIds);
    
    List<Venda> findByDataBetweenAndVendedorObjIdInAndFilialIdIn(LocalDate startDate, LocalDate endDate, List<Long> vendedorIds, List<Long> filialIds);
    
    // Métodos que filtram por time (String) - coluna 'filial' no banco
    @Query("SELECT v FROM Venda v WHERE v.data BETWEEN :startDate AND :endDate AND v.time IN :times")
    List<Venda> findByDataBetweenAndTimeIn(@Param("startDate") LocalDate startDate, 
                                            @Param("endDate") LocalDate endDate, 
                                            @Param("times") List<String> times);
    
    @Query("SELECT v FROM Venda v WHERE v.data BETWEEN :startDate AND :endDate AND v.vendedorObj.id IN :vendedorIds AND v.time IN :times")
    List<Venda> findByDataBetweenAndVendedorObjIdInAndTimeIn(@Param("startDate") LocalDate startDate, 
                                                               @Param("endDate") LocalDate endDate, 
                                                               @Param("vendedorIds") List<Long> vendedorIds, 
                                                               @Param("times") List<String> times);
}
