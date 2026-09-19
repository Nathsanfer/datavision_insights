package com.cti.provedor.model;

public class Insights {
    private String tipo;
    private String descricao;
    private String geradoEm;
    private Contrato contrato;

    // Constructor
    public Insights(String tipo, String descricao, String geradoEm, Contrato contrato) {
        this.tipo = tipo;
        this.descricao = descricao;
        this.geradoEm = geradoEm;
        this.contrato = contrato;
    }

    // Getters and Setters

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

    public String getGeradoEm() {
        return geradoEm;
    }

    public void setGeradoEm(String geradoEm) {
        this.geradoEm = geradoEm;
    }

    public Contrato getContrato() {
        return contrato;
    }

    public void setContrato(Contrato contrato) {
        this.contrato = contrato;
    }
}
