package com.cti.backend_java.models;

// Importação das bibliotecas

import org.hibernate.annotations.Check;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.math.BigDecimal;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity 
@Table(name = "contrato")
@Check(constraints = "data_fim IS NULL OR data_fim >= data_inicio")
@Check(constraints = "valor >= 0")

public class Contrato {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id_contrato")
    private Long idContrato;

    @ManyToOne 
    @JoinColumn (name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne 
    @JoinColumn (name = "id_servico", nullable = false)
    private Servico servico;

    @Column (name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column (name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column (name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column (name = "status", nullable = false)
    private StatusContrato status = StatusContrato.ATIVO;

    // Constructor

    public Contrato() {}

    public Contrato(Long idContrato, Cliente cliente, Servico servico, LocalDate dataInicio, LocalDate dataFim,
            BigDecimal valor, StatusContrato status) {
        this.idContrato = idContrato;
        this.cliente = cliente;
        this.servico = servico;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.valor = valor;
        this.status = status;
    }

    // Getters and Setters

    public Long getIdContrato() {
        return idContrato;
    }

    public void setIdContrato(Long idContrato) {
        this.idContrato = idContrato;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public StatusContrato getStatus() {
        return status;
    }

    public void setStatus(StatusContrato status) {
        this.status = status;
    }
    
}
