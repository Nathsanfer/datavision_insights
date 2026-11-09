package com.cti.backend_java.dto;

// Importação das classes de validação

import com.cti.backend_java.models.StatusUsuario;
import jakarta.validation.constraints.NotNull;

public record StatusUsuarioRequest(@NotNull StatusUsuario status) {}
