package com.cti.backend_java.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cti.backend_java.config.ApiException;

// Devolve ao front o JSON de estatísticas/insights que o Python gravou em estatistica_lote.
@RestController
@RequestMapping("/api/insights")
public class InsightsController {
    private final JdbcTemplate jdbc;

    public InsightsController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/ultimo")
    public ResponseEntity<String> ultimo() {
        return responder(jdbc.queryForList(
                "SELECT resultado::text FROM etl.estatistica_lote ORDER BY lote_id DESC LIMIT 1", String.class));
    }

    @GetMapping("/{loteId}")
    public ResponseEntity<String> porLote(@PathVariable long loteId) {
        return responder(jdbc.queryForList(
                "SELECT resultado::text FROM etl.estatistica_lote WHERE lote_id = ?", String.class, loteId));
    }

    private ResponseEntity<String> responder(List<String> linhas) {
        if (linhas.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Ainda não há insights gerados.");
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(linhas.get(0));
    }
}

