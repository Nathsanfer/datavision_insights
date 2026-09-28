package com.cti.backend_java.models;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import java.time.LocalDate;

@Entity
@Table(name = "insight")

public class Insight {
    
    // Atributos da Classe

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id_insight")
    private Long idInsight;

    @ManyToMany
    @JoinColumn (name= "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToMany 
    @JoinColumn (name= "id_contrato", nullable = false)
    private Contrato contrato;

    @Column (name = "tipo", nullable = false, length = 100)
    private String tipo;

    @Column (name = "descricao", nullable = false)
    private String descricao;

    @Column (name = "data_geracao", nullable = false)
    private LocalDate dataGeracao;

    // Constructors

    public Insight() {}

    public Insight(Long idInsight, Cliente cliente, Contrato contrato, String tipo, String descricao, LocalDate dataGeracao) {
        this.idInsight = idInsight;
        this.cliente = cliente;
        this.contrato = contrato;
        this.tipo = tipo;
        this.descricao = descricao;
        this.dataGeracao = dataGeracao;
    }

    // Getters and Setters

    public Long getIdInsight() {
        return idInsight;
    }

    public void setIdInsight(Long idInsight) {
        this.idInsight = idInsight;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Contrato getContrato() {
        return contrato;
    }

    public void setContrato(Contrato contrato) {
        this.contrato = contrato;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(LocalDate dataGeracao) {
        this.dataGeracao = dataGeracao;
    }

}
