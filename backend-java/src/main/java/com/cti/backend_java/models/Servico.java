package com.cti.backend_java.models;

// Importação das bibliotecas

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;

@Entity 
@Table(name = "servico")

public class Servico {

    // Atributos da Classe

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id_servico")
    private Long idServico;

    @Column (name = "nome", nullable = false,  length = 150)
    private String nome;

    @Column (name = "descricao", nullable = false)
    private String descricao;

    @Column (name = "categoria", nullable = false)
    private String categoria;

    @OneToMany 
    @JoinColumn (name = "id_contrato", nullable = false)
    private Contrato contrato;

    // Constructors

    public Servico() {}

    public Servico(String nome, String descricao, String categoria, Contrato contrato) {
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.contrato = contrato;
    }

    // Getters and Setters

    public Long getIdServico() {
        return idServico;
    }

    public void setIdServico(Long idServico) {
        this.idServico = idServico;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Contrato getContrato() {
        return contrato;
    }

    public void setContrato(Contrato contrato) {
        this.contrato = contrato;
    }

}
