package com.cti.backend_java.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.cti.backend_java.config.ApiException;
import com.cti.backend_java.config.AppProperties;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class PythonEtlService {
    private static final Logger log = LoggerFactory.getLogger(PythonEtlService.class);

    private final String url;
    private final String chave;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper json;

    public PythonEtlService(AppProperties props, ObjectMapper json) {
        this.url = props.etl() == null || props.etl().url() == null || props.etl().url().isBlank()
                ? "http://localhost:8000/etl" : props.etl().url().trim();
        this.chave = props.etl() == null || props.etl().key() == null ? "" : props.etl().key().trim();
        this.json = json;
    }

    public void enviar(String arquivoNome, int recebidas, int invalidas, int comAviso, List<Map<String, Object>> linhas) {
        Map<String, Object> pedido = new LinkedHashMap<>();
        pedido.put("arquivoNome", arquivoNome);
        pedido.put("recebidas", recebidas);
        pedido.put("invalidas", invalidas);
        pedido.put("comAviso", comAviso);
        pedido.put("linhas", linhas);
        log.info("Enviando {} linhas ao Python: POST {}", linhas.size(), url);
        try {
            HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofMinutes(5))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(json.writeValueAsBytes(pedido)));
            chamar(req);
        } catch (JacksonException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao montar o JSON: " + e.getMessage());
        }
    }

    private void chamar(HttpRequest.Builder req) {
        if (!chave.isEmpty()) {
            req.header("X-ETL-Key", chave);
        }
        try {
            HttpResponse<String> resp = http.send(req.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() != 200) {
                throw new ApiException(HttpStatus.BAD_GATEWAY,
                        "O serviço Python respondeu " + resp.statusCode() + ": " + resp.body());
            }
        } catch (IOException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Não foi possível falar com o serviço Python (ele está ligado? uvicorn api:app --port 8000).");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "A chamada ao Python foi interrompida.");
        }
    }
}
