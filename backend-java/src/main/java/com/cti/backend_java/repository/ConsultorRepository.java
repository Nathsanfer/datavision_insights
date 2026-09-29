package com.cti.backend_java.repository;

// Importação das bibliotecas

import org.springframework.data.jpa.repository.JpaRepository;
import com.cti.backend_java.models.Consultor;
import java.util.Optional;

public interface ConsultorRepository extends JpaRepository<Consultor, Long> {

    // Verificação de Email único
    
    Optional<Consultor> findByEmail(String email);

}
