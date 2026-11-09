package com.cti.backend_java.dto;

// Importação das classes de validação

import com.cti.backend_java.models.Perfil;
import jakarta.validation.constraints.NotNull;

public record PerfilRequest(@NotNull Perfil perfil) {}
