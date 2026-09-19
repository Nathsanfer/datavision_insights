package com.cti.provedor.model;

public class Consultor {
    private String nome;
    private String matricula;

    // Constructor
    public Consultor(String nome, String matricula) {
        this.nome = nome;
        this.matricula = matricula;
    }

    // Getters and Setters
    
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }
}
