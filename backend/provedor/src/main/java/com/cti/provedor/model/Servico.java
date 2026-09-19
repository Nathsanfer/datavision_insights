package com.cti.provedor.model;

public class Servico {
    private String nome;
    private String categoria;

    // Constructor
    public Servico(String nome, String categoria) {
        this.nome = nome;
        this.categoria = categoria;
    }

    // Getters and Setters

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}
