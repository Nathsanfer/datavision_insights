package com.cti.backend_java.repository;

// Importação das classes necessárias

import com.cti.backend_java.models.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Busca o usuário pelo email, ignorando maiúsculas e minúsculas

    Optional<Usuario> findByEmailIgnoreCase(String email);

    // Verifica se um usuário com o email fornecido já existe
    
    boolean existsByEmailIgnoreCase(String email);
}
