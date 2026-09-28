package com.cti.backend_java.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import java.time.LocalDate;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "telemetria")

public class Telemetria {
    
    // Atributos da Classe

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id_telemetria")
    private Long idTelemetria;

    @ManyToMany 
    @JoinColumn (name= "id_consultor", nullable = false)
    private Consultor consultor;

    @Column (name = "arquivo_name", nullable = false, length = 255)
    private String arquivoName;

    @Column (name = "data_hora", nullable = false)
    private LocalDate dataHora;

    @Enumerated (EnumType.STRING)
    @Column (name = "tipo_evento", nullable = false)
    private TipoEvento tipoEvento = TipoEvento.PROCESSAMENTO;

    @Enumerated (EnumType.STRING)
    @Column (name = "status", nullable = false)
    private StatusTelemetria status = StatusTelemetria.SUCESSO;

    @Column (name = "mensagem_erro", nullable = true)
    private String mensagemErro;

    // Construtors

    public Telemetria() {}

    public Telemetria(Long idTelemetria, Consultor consultor, String arquivoName, LocalDate dataHora, TipoEvento tipoEvento, StatusTelemetria status, String mensagemErro) {
        this.idTelemetria = idTelemetria;
        this.consultor = consultor;
        this.arquivoName = arquivoName;
        this.dataHora = dataHora;
        this.tipoEvento = tipoEvento;
        this.status = status;
        this.mensagemErro = mensagemErro;
    }

    // Getters and Setters

    public Long getIdTelemetria() {
        return idTelemetria;
    }

    public void setIdTelemetria(Long idTelemetria) {
        this.idTelemetria = idTelemetria;
    }

    public Consultor getConsultor() {
        return consultor;
    }

    public void setConsultor(Consultor consultor) {
        this.consultor = consultor;
    }

    public String getArquivoName() {
        return arquivoName;
    }

    public void setArquivoName(String arquivoName) {
        this.arquivoName = arquivoName;
    }

    public LocalDate getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDate dataHora) {
        this.dataHora = dataHora;
    }

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEvento tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public StatusTelemetria getStatus() {
        return status;
    }

    public void setStatus(StatusTelemetria status) {
        this.status = status;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public void setMensagemErro(String mensagemErro) {
        this.mensagemErro = mensagemErro;
    }
}
