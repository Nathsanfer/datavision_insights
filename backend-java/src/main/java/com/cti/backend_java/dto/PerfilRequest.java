package com.cti.backend_java.dto;

import com.cti.backend_java.models.Perfil;
import jakarta.validation.constraints.NotNull;

public record PerfilRequest(@NotNull Perfil perfil) {}
