package com.cti.backend_java.services;

import com.cti.backend_java.dto.*;
import com.cti.backend_java.models.Perfil;
import com.cti.backend_java.models.StatusUsuario;
import com.cti.backend_java.models.Usuario;
import com.cti.backend_java.repository.UsuarioRepository;
import com.cti.backend_java.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service // Regras de negócio do cadastro e do login.
public class AuthService {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository repository, PasswordEncoder encoder, JwtService jwt) {
        this.repository = repository;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public UsuarioResponse cadastrar(CadastroRequest dados) {
        String email = dados.email().trim().toLowerCase(Locale.ROOT);
        // BCrypt considera no máximo 72 bytes; comprimento não equivale a bytes em UTF-8.
        if (dados.senha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha muito longa para BCrypt");
        }
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome().trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(encoder.encode(dados.senha())); // Armazena apenas o hash.
        usuario.setPerfil(Perfil.USER); // Cadastro público nunca define ADMIN.
        usuario.setStatus(StatusUsuario.PENDENTE); // Só entra depois que o administrador aprovar.
        return UsuarioResponse.de(repository.save(usuario));
    }

    public LoginResponse entrar(LoginRequest dados) {
        Usuario usuario = repository.findByEmailIgnoreCase(dados.email().trim())
                .orElse(null);
        // Mesmo erro para usuário inexistente e senha inválida.
        if (usuario == null || !encoder.matches(dados.senha(), usuario.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos");
        }
        // Só verifica o status depois da senha, para não revelar contas existentes a quem não as conhece.
        if (usuario.getStatus() == StatusUsuario.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seu cadastro aguarda aprovação do administrador");
        }
        if (usuario.getStatus() != StatusUsuario.ATIVO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seu acesso está bloqueado. Procure o administrador");
        }
        return new LoginResponse(jwt.gerar(usuario), "Bearer", UsuarioResponse.de(usuario));
    }

    // Atualiza os dados do próprio usuário. O token usa o e-mail como identidade, então é reemitido.
    public LoginResponse atualizarMeusDados(String emailAtual, AtualizarMeRequest dados) {
        Usuario usuario = repository.findByEmailIgnoreCase(emailAtual)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        String novoEmail = dados.email().trim().toLowerCase(Locale.ROOT);
        if (!novoEmail.equalsIgnoreCase(usuario.getEmail()) && repository.existsByEmailIgnoreCase(novoEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
        // Trocar a senha exige a senha atual, para que um token roubado não altere a conta.
        if (dados.novaSenha() != null && !dados.novaSenha().isBlank()) {
            if (dados.senhaAtual() == null || !encoder.matches(dados.senhaAtual(), usuario.getSenhaHash())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta");
            }
            if (dados.novaSenha().getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha muito longa para BCrypt");
            }
            usuario.setSenhaHash(encoder.encode(dados.novaSenha()));
        }
        usuario.setNome(dados.nome().trim());
        usuario.setEmail(novoEmail);
        Usuario salvo = repository.save(usuario);
        return new LoginResponse(jwt.gerar(salvo), "Bearer", UsuarioResponse.de(salvo));
    }
}
