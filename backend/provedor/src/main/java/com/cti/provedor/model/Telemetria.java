package com.cti.provedor.model;

public class Telemetria {
    private String evento;
    private String status;
    private String timestamp;
    private Servico servico;

    // Constructor
    public Telemetria(String evento, String status, String timestamp, Servico servico) {
        this.evento = evento;
        this.status = status;
        this.timestamp = timestamp;
        this.servico = servico;
    }

    // Getters and Setters

    public String getEvento() {
        return evento;
    }

    public void setEvento(String evento) {
        this.evento = evento;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }
}