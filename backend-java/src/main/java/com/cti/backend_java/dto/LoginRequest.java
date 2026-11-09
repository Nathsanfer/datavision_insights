package com.cti.backend_java.dto;

// Importação das classes de validação

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

    // Recebe os campos do login

    @NotBlank String email,
    @NotBlank String senha
) {}
