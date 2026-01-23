package com.example.vendasjaragua.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "financeiro_clientes")
@Data
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome")
    private String cliente; // Mapeia para 'nome' na tabela financeiro_clientes
    
    @Column(name = "cpf_cnpj")
    private String nf; // CPF/CNPJ do cliente
    
    private String ov; // Ordem de Venda
    
    private String entrega; // Informação de entrega
    
    private String telefone;
    
    private String cidade; // Cidade do cliente
    
    private String estado; // Estado (UF) do cliente
    
    private String vendedor;

    private LocalDate data; // Data da venda/cadastro

    private String placas; // Informação sobre placas solares
    
    private String inversor; // Tipo/modelo do inversor
    
    private String potencia; // Potência do sistema

    @Column(name = "valor_debito")
    private BigDecimal valorVenda; // Mapeia para valor_debito

    @Column(name = "valor_custo")
    private BigDecimal valorMaterial; // Mapeia para valor_custo (custo/material)

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<VendaItem> produto; // Lista de produtos em JSON

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "inverter_info", columnDefinition = "jsonb")
    private List<InverterItem> inverterInfo = new ArrayList<>(); // Info dos inversores em JSON

    @Column(name = "filial")
    private String time; // Mapeia para 'filial' (substitui time)
    
    // Novos campos da tabela financeiro_clientes
    private String email;
    
    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;
    
    @Column(columnDefinition = "TEXT")
    private String observacao;
    
    @Column(name = "forma_pagamento")
    private String formaPagamento;
    
    private Boolean ganho = true; // Padrão: true para novas vendas

    @PrePersist
    public void prePersist() {
        if (dataCadastro == null) {
            dataCadastro = LocalDateTime.now();
        }
        if (valorVenda == null) {
            valorVenda = BigDecimal.ZERO;
        }
        if (valorMaterial == null) {
            valorMaterial = BigDecimal.ZERO;
        }
        if (ganho == null) {
            ganho = true; // Padrão: true
        }
    }

    // Saldo = valor_debito - valor_pago
    public BigDecimal getValorBruto() {
        if (valorVenda != null && valorMaterial != null) {
            return valorVenda.subtract(valorMaterial);
        }
        return BigDecimal.ZERO;
    }

    public Double getMarkup() {
        if (valorVenda != null && valorMaterial != null && valorMaterial.compareTo(BigDecimal.ZERO) != 0) {
            try {
                return valorVenda.divide(valorMaterial, 4, java.math.RoundingMode.HALF_UP)
                        .subtract(BigDecimal.ONE)
                        .doubleValue();
            } catch (ArithmeticException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
    
    // Método auxiliar para saber se está pago
    public boolean isPago() {
        return getValorBruto().compareTo(BigDecimal.ZERO) <= 0;
    }
}
