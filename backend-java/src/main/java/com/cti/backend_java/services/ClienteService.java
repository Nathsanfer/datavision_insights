package com.cti.backend_java.services;

// Importação das bibliotecas 
import org.springframework.stereotype.Service;
import com.cti.backend_java.repository.ClienteRepository;
import com.cti.backend_java.repository.ConsultorRepository;
import com.cti.backend_java.models.Cliente;
import com.cti.backend_java.models.Consultor;
import jakarta.transaction.Transactional;

@Service
public class ClienteService {
    
    private  final ClienteRepository clienteRepository;
    private  final ConsultorRepository consultorRepository;

    // Constructor 

    public ClienteService(
        ClienteRepository clienteRepository,
        ConsultorRepository consultorRepository
    ){
        this.clienteRepository = clienteRepository;
        this.consultorRepository = consultorRepository;
    }

    // POST - CRIAR ====

    @Transactional
    public Cliente criar(Long idConsultor, Cliente cliente) {

        Consultor consultor = consultorRepository.findById(idConsultor)
                .orElseThrow(() -> new RuntimeException("Consultor não encontrado"));

        if (cliente.getNomeEmpresa() == null || cliente.getNomeEmpresa().isBlank()) {
            throw new RuntimeException("Nome da empresa é obrigatório");
        }

        if (cliente.getSegmento() == null || cliente.getSegmento().isBlank()) {
            throw new RuntimeException("Segmento é obrigatório");
        }

        if (cliente.getFaturamentoAnual() == null) {
            throw new RuntimeException("Faturamento anual é obrigatório");
        }

        if (cliente.getNivel() == null) {
            throw new RuntimeException("Nível do cliente é obrigatório");
        }

        if (cliente.getStatus() == null) {
            throw new RuntimeException("Status do cliente é obrigatório");
        }

        cliente.setConsultor(consultor);
        return clienteRepository.save(cliente);

    }

    // GET - LISTAR POR ID ====

    public Cliente buscarPorId(Long id) {
        
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }

    // PUT - ATUALIZAR ====

    @Transactional 
    public  Cliente atualizar(Long id, Cliente dados){
        Cliente cliente = buscarPorId(id);

        cliente.setNomeEmpresa(dados.getNomeEmpresa());
        cliente.setSegmento(dados.getSegmento());
        cliente.setFaturamentoAnual(dados.getFaturamentoAnual());
        cliente.setNivel(dados.getNivel());
        cliente.setStatus(dados.getStatus());

        return clienteRepository.save(cliente);
    }

    // DELETE

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarPorId(id);
        clienteRepository.deleteById(cliente.getIdCliente());
    }
}
