package com.cti.backend_java.controllers;

// Importação das bibliotecas

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.cti.backend_java.models.Consultor;
import com.cti.backend_java.services.ConsultorService;

@RestController
@RequestMapping ("/consultor")
public class ConsultorController {
 
    private final ConsultorService service;

    // Constructor

    public ConsultorController(ConsultorService service) {
        this.service = service;
    }

    // POST - CRIAR ====

    @PostMapping 
    public Consultor criar (@RequestBody Consultor consultor) {
        return service.criar(consultor);
    }

    // LOGIN - AUTENTICAÇÃO ====

    @PostMapping ("/login")
    public Consultor login (@RequestBody Consultor consultor) {
        return service.login(consultor.getEmail(), consultor.getSenha());
    }

    // GET - LISTAR ====

    @GetMapping 
    public List<Consultor> listar () {
        return service.listar();
    }

    // GET - LISTAR POR ID ====

    @GetMapping ("/{id}")
    public Consultor buscar (@PathVariable Long id) {
        return service.buscar(id);
    }

    // PUT - ATUALIZAR ====

    @PutMapping ("/{id}")
    public Consultor atualizar (@PathVariable Long id, @RequestBody Consultor consultor) {
        return service.atualizar(id, consultor);
    }

    // DELETE - DELETAR ====

    @DeleteMapping ("/{id}")
    public void excluir (@PathVariable Long id) {
        service.excluir(id);
    }

}