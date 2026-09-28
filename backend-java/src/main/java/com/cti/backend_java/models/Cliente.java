package com.cti.backend_java.models;

// Importação das bibliotecas

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.math.BigDecimal;

@Entity 
@Table(name = "cliente")

public class Cliente {

    // Atributos da Classe

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id_cliente")
    private Long idCliente;

    @ManyToOne 
    @JoinColumn (name = "id_consultor", nullable = false)
    private Consultor consultor;

    @Column (name = "nome_empresa", nullable = false, length = 180)
    private String nomeEmpresa;

    @Column (name = "segmento", nullable = false, length = 100)
    private String segmento;

    @Column (name = "faturamento_anual", nullable = false, precision = 15, scale = 2)
    private BigDecimal faturamentoAnual;

    @Enumerated(EnumType.STRING)
    @Column (name = "nivel", nullable = false)
    private NivelCliente nivel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusCliente status = StatusCliente.ATIVO;

    // Constructors

    public Cliente() {}

    public Cliente(Consultor consultor, String nomeEmpresa, String segmento, BigDecimal faturamentoAnual, NivelCliente nivel, StatusCliente status) {
        this.consultor = consultor;
        this.nomeEmpresa = nomeEmpresa;
        this.segmento = segmento;
        this.faturamentoAnual = faturamentoAnual;
        this.nivel = nivel;
        this.status = status;
    }

    // Getters and Setters

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Consultor getConsultor() {
        return consultor;
    }

    public void setConsultor(Consultor consultor) {
        this.consultor = consultor;
    }

    public String getNomeEmpresa() {
        return nomeEmpresa;
    }

    public void setNomeEmpresa(String nomeEmpresa) {
        this.nomeEmpresa = nomeEmpresa;
    }

    public String getSegmento() {
        return segmento;
    }

    public void setSegmento(String segmento) {
        this.segmento = segmento;
    }

    public BigDecimal getFaturamentoAnual() {
        return faturamentoAnual;
    }

    public void setFaturamentoAnual(BigDecimal faturamentoAnual) {
        this.faturamentoAnual = faturamentoAnual;
    }

    public NivelCliente getNivel() {
        return nivel;
    }

    public void setNivel(NivelCliente nivel) {
        this.nivel = nivel;
    }

    public StatusCliente getStatus() {
        return status;
    }

    public void setStatus(StatusCliente status) {
        this.status = status;
    }

}