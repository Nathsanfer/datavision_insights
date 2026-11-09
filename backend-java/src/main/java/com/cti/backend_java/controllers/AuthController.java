package com.cti.backend_java.controllers;

// Importação das blibliotecas e classes necessários

import com.cti.backend_java.models.Usuario;
import com.cti.backend_java.repository.UsuarioRepository;
import com.cti.backend_java.dto.*;
import com.cti.backend_java.services.AuthService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService service;
    private final UsuarioRepository repository;

    // Constructor 

    public AuthController(AuthService service, UsuarioRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    // POST - Cadastro de novos usuários

    @PostMapping("/auth/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@Valid @RequestBody CadastroRequest dados) {
        return service.cadastrar(dados); // Controller delega a regra ao Service.
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> dadosInvalidos(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Dados inválidos");
        return ResponseEntity.badRequest().body(Map.of("message", mensagem));
    }

    // POST - Login de usuários 

    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest dados) {
        return service.entrar(dados);
    }

    // GET - Retorna os dados do usuário autenticado

    @GetMapping("/auth/me")
    public UsuarioResponse me(Principal principal) {
        Usuario usuario = repository.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return UsuarioResponse.de(usuario);
    }

    // PUT - Atualiza os dados do usuário autenticado

    @PutMapping("/auth/me")
    public LoginResponse atualizarMe(Principal principal, @Valid @RequestBody AtualizarMeRequest dados) {
        return service.atualizarMeusDados(principal.getName(), dados);
    }

    // GET - Teste de Rotas Públicas e Privadas

    @GetMapping("/publico")
    public Map<String, String> publico() { return Map.of("mensagem", "Rota pública"); }

    @GetMapping("/privado")
    public Map<String, String> privado() { return Map.of("mensagem", "JWT válido!"); }
}
