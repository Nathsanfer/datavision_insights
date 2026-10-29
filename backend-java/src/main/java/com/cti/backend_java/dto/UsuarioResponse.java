package com.cti.backend_java.dto;

import com.cti.backend_java.models.Perfil;
import com.cti.backend_java.models.StatusUsuario;
import com.cti.backend_java.models.Usuario;

public record UsuarioResponse(Long id, String nome, String email, Perfil perfil, StatusUsuario status) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(), u.getStatus());
    }
}
