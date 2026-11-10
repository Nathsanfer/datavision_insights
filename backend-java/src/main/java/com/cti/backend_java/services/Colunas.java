package com.cti.backend_java.services;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;

public class Colunas {
    private Colunas() {}  

    public static final String CODIGO = "codigoCliente";
    public static final String NOME = "nomeCliente";
    public static final String CONSULTOR = "consultor";
    public static final String SEGMENTO = "segmento";
    public static final String NIVEL = "nivel";
    public static final String FATURAMENTO = "faturamento";
    public static final String SERVICOS = "servicos";
    public static final String DATA = "data";
    public static final String CIDADE = "cidade";
    public static final String UF = "uf";

    public static final List<String> TODAS = List.of(CODIGO, NOME, CONSULTOR, SEGMENTO, NIVEL, FATURAMENTO, SERVICOS, DATA, CIDADE, UF);

    public static final List<String> OBRIGATORIAS = List.of(CODIGO, NOME, CONSULTOR, SEGMENTO, NIVEL, FATURAMENTO, SERVICOS, DATA);

    public static final Map<String, Integer> TAMANHO_MAXIMO = Map.of(
        CODIGO, 60,
        NOME, 255,
        CONSULTOR, 120,
        SEGMENTO, 80,
        NIVEL, 20,
        SERVICOS, 400,
        CIDADE, 120,
        UF, 10
    );

    public static String normalizarCabecalho(String texto) {
        if (texto == null) return "";
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return semAcento.trim().toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
    }
}
