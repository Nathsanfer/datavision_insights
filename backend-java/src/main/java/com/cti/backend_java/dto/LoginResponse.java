package com.cti.backend_java.dto;

public record LoginResponse(
    String token, 
    String tipo, 
    UsuarioResponse usuario
) {}