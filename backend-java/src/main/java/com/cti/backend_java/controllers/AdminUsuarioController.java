package com.cti.backend_java.controllers;

import com.cti.backend_java.dto.PerfilRequest;
import com.cti.backend_java.dto.StatusUsuarioRequest;
import com.cti.backend_java.dto.UsuarioResponse;
import com.cti.backend_java.models.Usuario;
import com.cti.backend_java.repository.UsuarioRepository;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/user")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {
    private final UsuarioRepository repository;

    public AdminUsuarioController(UsuarioRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/user")
    public List<UsuarioResponse> listar() {
        return repository.findAll().stream()
                .map(UsuarioResponse::de)
                .toList();
    }

    // Aprova (ATIVO) ou recusa/bloqueia (BLOQUEADO) uma solicitação de cadastro.
    @PatchMapping("/{id}/status")
    public UsuarioResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody StatusUsuarioRequest dados,
                                         Principal principal) {
        Usuario usuario = buscar(id);
        bloquearAutoAlteracao(usuario, principal, "Você não pode alterar o próprio status");
        usuario.setStatus(dados.status());
        return UsuarioResponse.de(repository.save(usuario));
    }

    // Altera o perfil de acesso (USER ou ADMIN).
    @PatchMapping("/{id}/perfil")
    public UsuarioResponse alterarPerfil(@PathVariable Long id, @Valid @RequestBody PerfilRequest dados,
                                         Principal principal) {
        Usuario usuario = buscar(id);
        bloquearAutoAlteracao(usuario, principal, "Você não pode alterar o próprio perfil");
        usuario.setPerfil(dados.perfil());
        return UsuarioResponse.de(repository.save(usuario));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, Principal principal) {
        Usuario usuario = buscar(id);
        bloquearAutoAlteracao(usuario, principal, "Você não pode excluir a própria conta por aqui");
        repository.delete(usuario);
    }

    private Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    // Evita que o administrador se bloqueie, rebaixe ou remova e fique sem acesso ao sistema.
    private void bloquearAutoAlteracao(Usuario usuario, Principal principal, String mensagem) {
        if (usuario.getEmail().equalsIgnoreCase(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
    }
}
