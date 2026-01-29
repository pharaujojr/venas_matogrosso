package com.example.vendasjaragua.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface VendaRepositoryCustom {
    List<Map<String, Object>> findFaturamentoPorGrupoDynamic(LocalDate inicio, LocalDate fim, 
                                                  List<String> times, 
                                                  List<String> vendedores, 
                                                  List<String> grupos, 
                                                  List<String> produtos);
    
    List<Map<String, Object>> findVendasPorMes(LocalDate inicio, LocalDate fim,
                                    List<String> times, 
                                    List<String> vendedores,
                                    List<String> grupos,
                                    List<String> produtos,
                                    Boolean closing,
                                    Boolean ganho);
    
    long countVendasFiltradas(LocalDate inicio, LocalDate fim,
                             List<String> times, 
                             List<String> vendedores,
                             List<String> grupos,
                             List<String> produtos,
                             Boolean closing,
                             Boolean ganho);
    
    long countVendasComGanhoFiltradas(LocalDate inicio, LocalDate fim,
                                      List<String> times, 
                                      List<String> vendedores,
                                      List<String> grupos,
                                      List<String> produtos,
                                      Boolean closing);
    
    List<Map<String, Object>> findVendasPorVendedorDynamic(LocalDate inicio, LocalDate fim,
                                                List<String> times, 
                                                List<String> vendedores,
                                                List<String> grupos,
                                                List<String> produtos,
                                                Boolean closing,
                                                Boolean ganho);
}
