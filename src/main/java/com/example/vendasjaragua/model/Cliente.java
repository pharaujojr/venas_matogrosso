package com.example.vendasjaragua.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "financeiro_clientes")
@Data
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private LocalDate data;
    
    private String email;
    
    private String telefone;
    
    @Column(name = "cpf_cnpj", length = 20)
    private String cpfCnpj;

    @Column(nullable = false, length = 50)
    private String filial;

    @Column(name = "valor_debito", nullable = false, precision = 19, scale = 2)
    private BigDecimal valorDebito = BigDecimal.ZERO;

    @Column(name = "valor_pago", nullable = false, precision = 19, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro = LocalDateTime.now();

    @Column(length = 25)
    private String vendedor;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "forma_pagamento", length = 50)
    private String formaPagamento;

    @Column(nullable = false)
    private Boolean ganho = false;

    @PrePersist
    public void prePersist() {
        if (dataCadastro == null) {
            dataCadastro = LocalDateTime.now();
        }
        if (valorDebito == null) {
            valorDebito = BigDecimal.ZERO;
        }
        if (valorPago == null) {
            valorPago = BigDecimal.ZERO;
        }
        if (ganho == null) {
            ganho = false;
        }
    }

    // Métodos auxiliares
    public BigDecimal getSaldo() {
        if (valorDebito != null && valorPago != null) {
            return valorDebito.subtract(valorPago);
        }
        return BigDecimal.ZERO;
    }

    public boolean isPago() {
        return getSaldo().compareTo(BigDecimal.ZERO) <= 0;
    }
}
