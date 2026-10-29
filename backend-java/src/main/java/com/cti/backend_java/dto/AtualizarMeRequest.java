package com.cti.backend_java.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Dados que o próprio usuário pode alterar. Nova senha é opcional; se informada, exige a senha atual.
public record AtualizarMeRequest(
    @NotBlank @Size(max = 120) String nome,
    @NotBlank @Email @Size(max = 160) String email,
    String senhaAtual,
    @Pattern(regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d).{8,72}$", message = "Use de 8 a 72 caracteres com letras e números") String novaSenha
) {}
