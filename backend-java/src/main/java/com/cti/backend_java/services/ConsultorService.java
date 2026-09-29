package com.cti.backend_java.services;

// Importação das bibliotecas

import java.util.List;
import org.springframework.stereotype.Service;
import com.cti.backend_java.repository.ConsultorRepository;
import jakarta.transaction.Transactional;
import com.cti.backend_java.models.Consultor;

@Service
public class ConsultorService {

    private final ConsultorRepository repository;

    // Constructor

    public ConsultorService(ConsultorRepository repository) {
        this.repository = repository;
    }

    // POST - CRIAR ====

    @Transactional
    public Consultor criar(Consultor consultor) {

        if (consultor.getNome() == null || consultor.getNome().isBlank()) {
            throw new RuntimeException("Nome é obrigatório");
        }

        if (consultor.getEmail() == null || consultor.getEmail().isBlank()) {
            throw new RuntimeException("Email é obrigatório");
        }

        if (consultor.getSenha() == null || consultor.getSenha().isBlank()) {
            throw new RuntimeException("Senha é obrigatória");
        }

        // Verificação de Email único

        if (repository.findByEmail(consultor.getEmail()).isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }

        return repository.save(consultor);
    }

    // LOGIN - AUTENTICAÇÃO ====

    public Consultor login(String email, String senha) {

        Consultor consultor = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Consultor não encontrado"));

        if (!consultor.getSenha().equals(senha)) {
            throw new RuntimeException("Senha incorreta");
        }

        return consultor;

    }

    // GET - LISTAR ====

    public List<Consultor> listar() {
        return repository.findAll();
    }

    // GET - LISTAR POR ID ====

    public Consultor buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Consultor não encontrado"));
    }

    // PUT - ATUALIZAR ====

    @Transactional
    public Consultor atualizar(Long id, Consultor dados) {

        Consultor consultor = buscar(id);

        consultor.setNome(dados.getNome());
        consultor.setEmail(dados.getEmail());
        consultor.setSenha(dados.getSenha());

        return repository.save(consultor);

    }

    // DELETE - DELETAR ====

    @Transactional
    public void excluir(Long id) {
        Consultor consultor = buscar(id);

        repository.deleteById(
                consultor.getIdConsultor());
    }

}
