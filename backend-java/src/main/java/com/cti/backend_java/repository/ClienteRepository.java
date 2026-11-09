package com.cti.backend_java.repository;

// Importação das dependências necessárias

import org.springframework.data.jpa.repository.JpaRepository;
import com.cti.backend_java.models.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
}
