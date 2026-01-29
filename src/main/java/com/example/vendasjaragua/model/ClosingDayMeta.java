package com.example.vendasjaragua.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "closing_day_metas")
public class ClosingDayMeta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "filial_nome", nullable = false, unique = true)
    private String filialNome;
    
    @Column(name = "meta_valor", nullable = false)
    private BigDecimal metaValor = BigDecimal.ZERO;
    
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
    
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFilialNome() {
        return filialNome;
    }
    
    public void setFilialNome(String filialNome) {
        this.filialNome = filialNome;
    }
    
    public BigDecimal getMetaValor() {
        return metaValor;
    }
    
    public void setMetaValor(BigDecimal metaValor) {
        this.metaValor = metaValor;
    }
    
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    
    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
    
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}
