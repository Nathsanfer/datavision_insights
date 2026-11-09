package com.cti.backend_java.dto;

public record LoginResponse(

    // Recebe os campos do login com o token

    String token, 
    String tipo, 
    UsuarioResponse usuario
) {}