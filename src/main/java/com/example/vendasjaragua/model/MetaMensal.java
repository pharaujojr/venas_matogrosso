package com.example.vendasjaragua.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "metas_mensais", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"filial_id", "mes", "ano"})
})
public class MetaMensal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "filial_id")
    private Filial filial;
    
    @Column(name = "mes", nullable = false)
    private Integer mes;
    
    @Column(name = "ano", nullable = false)
    private Integer ano;
    
    @Column(name = "meta_valor", nullable = false, precision = 15, scale = 2)
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
    
    public Filial getFilial() {
        return filial;
    }
    
    public void setFilial(Filial filial) {
        this.filial = filial;
    }
    
    public Integer getMes() {
        return mes;
    }
    
    public void setMes(Integer mes) {
        this.mes = mes;
    }
    
    public Integer getAno() {
        return ano;
    }
    
    public void setAno(Integer ano) {
        this.ano = ano;
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
