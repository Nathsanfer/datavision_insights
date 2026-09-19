package com.cti.provedor.model;

public class Contrato {
    private String dataInicio;
    private String status;
    private Cliente cliente;
    private Servico servico;

    // Constructor
    public Contrato(String dataInicio, String status, Cliente cliente, Servico servico) {
        this.dataInicio = dataInicio;
        this.status = status;
        this.cliente = cliente;
        this.servico = servico;
    }

    // Getters and Setters

    public String getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(String dataInicio) {
        this.dataInicio = dataInicio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
