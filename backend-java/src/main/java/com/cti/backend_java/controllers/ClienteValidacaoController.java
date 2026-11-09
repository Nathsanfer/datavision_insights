package com.cti.backend_java.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.cti.backend_java.dto.ClienteDTO;
import com.cti.backend_java.services.ClienteValidacaoService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController 
@RequestMapping("/api/clientes")

public class ClienteValidacaoController {
    
    private final ClienteValidacaoService service;

    public ClienteValidacaoController(ClienteValidacaoService service) {
        this.service = service;
    }

    @PostMapping("/validar")
    public ResponseEntity<List<ClienteDTO>> validarClientes(@RequestBody List<@Valid ClienteDTO> clientes) {
        List<ClienteDTO> preparados = service.validarEPreparar(clientes);
        return ResponseEntity.ok(preparados);
    }
    
}