package com.cti.provedor.model;

public class Cliente {
    private String codigo;
    private String segmento;
    private String nivelAtendimento;
    private Consultor consultor;

    // Constructor
    public Cliente(String codigo, String segmento, String nivelAtendimento, Consultor consultor) {
        this.codigo = codigo;
        this.segmento = segmento;
        this.nivelAtendimento = nivelAtendimento;
        this.consultor = consultor;
    }

    // Getters and Setters

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getSegmento() {
        return segmento;
    }

    public void setSegmento(String segmento) {
        this.segmento = segmento;
    }

    public String getNivelAtendimento() {
        return nivelAtendimento;
    }

    public void setNivelAtendimento(String nivelAtendimento) {
        this.nivelAtendimento = nivelAtendimento;
    }

    public Consultor getConsultor() {
        return consultor;
    }

    public void setConsultor(Consultor consultor) {
        this.consultor = consultor;
    }
}
