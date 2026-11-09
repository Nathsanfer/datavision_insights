package com.cti.backend_java.services;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cti.backend_java.dto.ClienteDTO;

@Service 
public class ClienteValidacaoService {
    
    public List<ClienteDTO> validarEPreparar(List<ClienteDTO> clientes) {
        Set<String> codigos = new HashSet<>();

        for (ClienteDTO cliente : clientes) {
            String codigo = cliente.codigoCliente().trim();

            if(!codigos.add(codigo)) {
                throw new IllegalArgumentException("Código repetido na planilha!" + codigo);

            }
        }
        
        return clientes.stream()
            .map(this::padronizar)
            .toList();
    }

    private ClienteDTO padronizar(ClienteDTO c) {
        return new ClienteDTO(
            c.codigoCliente().trim(),
            c.nomeCliente().trim(),
            formatarNome(c.consultor()),
            padronizarSegmento(c.segmentoCliente()),
            c.nivelCliente().trim().toUpperCase(),
            c.faturamentoAnual(),
            c.servicoContratados().trim(),
            c.dataContratacao(),
            c.cidade() == null ? "" : c.cidade().trim(),
            c.uf() == null ? "" : c.uf().trim().toUpperCase()
        );
    }

    private String padronizarSegmento(String segmento) {
        String s = segmento.trim().toLowerCase();

        if (s.equals("ind.") || s.equals("industrial")) {
            return "Indústrial";
        } 
        if (s.equals("comercio") || s.equals("comércio")) {
            return "Comércio";
        } 
        if (s.equals("servicos") || s.equals("serviços")) {
            return "Serviços";
        } 
        return segmento.trim();
    }

    private String formatarNome(String nome) {
        String texto = nome.trim().toLowerCase();
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }
}
