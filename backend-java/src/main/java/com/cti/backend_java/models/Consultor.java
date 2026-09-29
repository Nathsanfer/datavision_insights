package com.cti.backend_java.models;

// Importação das bibliotecas

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import java.util.List;
import java.util.ArrayList;

@Entity 
@Table (name = "consultor")

public class Consultor {
    
    // Atributos da Classe

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id_consultor")
    private Long idConsultor;

    @Column (name = "nome", nullable = false, length = 150)
    private String nome;

    @Column (name = "email", nullable = false, length = 150, unique = true)
    private String email;

    @Column (name = "senha", nullable = false)
    private String senha;

    @Column (name = "telefone", nullable = false, length = 30)
    private String telefone;

    @OneToMany (mappedBy = "consultor")
    private List<Cliente> clientes = new ArrayList<>();

    // Constructors

    public Consultor() {}

    public Consultor(String nome, String email, String senha,String telefone) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
    }

    // Getters and Setters

    public Long getIdConsultor() {
        return idConsultor;
    }

    public void setIdConsultor(Long idConsultor) {
        this.idConsultor = idConsultor;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    // Getters and Setters para a lista

    public List<Cliente> getClientes() {
        return clientes;
    }

    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes;
    }

}
