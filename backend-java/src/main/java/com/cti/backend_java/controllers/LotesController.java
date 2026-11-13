package com.cti.backend_java.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cti.backend_java.config.ApiException;

// Histórico dos uploads (lotes gravados pelo Python no schema etl).
@RestController
@RequestMapping("/api/lotes")
public class LotesController {
    private final JdbcTemplate jdbc;

    public LotesController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<Map<String, Object>> listar() {
        return jdbc.queryForList("""
                SELECT l.id,
                       l.arquivo_nome AS "arquivoNome",
                       to_char(l.enviado_em, 'YYYY-MM-DD"T"HH24:MI:SS"Z"') AS "enviadoEm",
                       l.status,
                       l.total_linhas AS "totalLinhas",
                       l.linhas_validas AS "linhasValidas",
                       l.linhas_invalidas AS "linhasInvalidas",
                       l.linhas_com_aviso AS "linhasComAviso",
                       l.mensagem,
                       (e.resultado->'resumo'->>'faturamentoTotal')::numeric AS "faturamentoTotal",
                       (e.resultado->'resumo'->>'totalClientes')::int AS "totalClientes"
                FROM etl.upload_lote l
                LEFT JOIN etl.estatistica_lote e ON e.lote_id = l.id
                ORDER BY l.id DESC
                """);
    }

    // Só ADMIN (ver SecurityConfig). A exclusão em cascata remove clientes, contratos e estatísticas.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable long id) {
        int apagados = jdbc.update("DELETE FROM etl.upload_lote WHERE id = ?", id);
        if (apagados == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Lote não encontrado.");
        }
    }
}
