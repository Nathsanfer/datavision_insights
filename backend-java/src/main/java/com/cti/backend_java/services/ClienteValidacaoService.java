package com.cti.backend_java.services;

import static com.cti.backend_java.services.Colunas.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.cti.backend_java.dto.ClienteDTO;
import com.cti.backend_java.dto.UploadDtos.LinhaEnvio;

@Service
public class ClienteValidacaoService {

    public record ResultadoLinha(int numero, Map<String, String> valores, List<String> erros, List<String> avisos) {
        public boolean valida() {
            return erros.isEmpty();
        }
    }

    private static final Pattern RE_CODIGO = Pattern.compile("^[A-Za-z0-9_-]{1,60}$");
    private static final Pattern RE_UF = Pattern.compile("^[A-Za-z]{2}$");
    private static final Pattern DATA_ISO = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})([T ].*)?$");
    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("d/M/uuuu");
    private static final BigDecimal LIMITE_FATURAMENTO = new BigDecimal("1000000000000");
    private static final Set<String> NIVEIS = Set.of("A", "B", "C");

    public List<ResultadoLinha> validarLinhas(List<LinhaEnvio> linhas) {
        List<ResultadoLinha> saida = new ArrayList<>();
        Map<String, Integer> codigos = new HashMap<>();
        Map<String, Integer> conteudos = new HashMap<>();
        Map<String, String> codigoDoConteudo = new HashMap<>();
        LocalDate hoje = LocalDate.now();

        for (int i = 0; i < linhas.size(); i++) {
            LinhaEnvio linha = linhas.get(i);
            int numero = linha.numeroLinha() == null ? i + 2 : linha.numeroLinha();
            Map<String, String> entrada = mapear(linha);
            List<String> erros = new ArrayList<>();
            List<String> avisos = new ArrayList<>();
            Map<String, String> valores = new LinkedHashMap<>();

            for (String campo : TODAS) {
                String bruto = entrada.get(campo);
                String limpo = bruto == null ? "" : bruto.strip().replaceAll("\\s{2,}", " ");
                if (bruto != null && !limpo.equals(bruto)) {
                    avisos.add(campo + ": espaços extras removidos");
                }
                valores.put(campo, limpo);
                Integer max = TAMANHO_MAXIMO.get(campo);
                if (max != null && limpo.length() > max) {
                    erros.add(campo + ": excede " + max + " caracteres");
                    valores.put(campo, limpo.substring(0, max));
                }
            }

            for (String campo : OBRIGATORIAS) {
                if (valores.get(campo).isEmpty()) {
                    erros.add(campo + ": campo obrigatório vazio");
                }
            }

            String codigo = valores.get(CODIGO);
            if (!codigo.isEmpty()) {
                if (!RE_CODIGO.matcher(codigo).matches()) {
                    erros.add(CODIGO + ": use apenas letras, números, '-' ou '_'");
                }
                String chave = codigo.toUpperCase();
                Integer primeira = codigos.putIfAbsent(chave, numero);
                if (primeira != null) {
                    erros.add(CODIGO + ": código duplicado (já usado na linha " + primeira + ")");
                }
            }

            String segmento = valores.get(SEGMENTO);
            if (!segmento.isEmpty()) {
                String padrao = padronizarSegmento(segmento);
                if (padrao == null) {
                    erros.add(SEGMENTO + ": '" + segmento + "' não reconhecido (use Indústria, Comércio, Serviços, Saúde, Educação ou Tecnologia)");
                } else if (!padrao.equals(segmento)) {
                    avisos.add(SEGMENTO + ": '" + segmento + "' será padronizado para " + padrao);
                }
            }

            String consultor = valores.get(CONSULTOR);
            if (!consultor.isEmpty()) {
                String ajustado = formatarNome(consultor);
                if (!ajustado.equals(consultor)) {
                    avisos.add(CONSULTOR + ": '" + consultor + "' será padronizado para " + ajustado);
                }
            }

            String nivel = valores.get(NIVEL);
            if (!nivel.isEmpty()) {
                String maiusculo = nivel.toUpperCase();
                if (!NIVEIS.contains(maiusculo)) {
                    erros.add(NIVEL + ": '" + nivel + "' inválido (use A, B ou C)");
                } else if (!maiusculo.equals(nivel)) {
                    avisos.add(NIVEL + ": '" + nivel + "' será convertido para " + maiusculo);
                }
            }

            String faturamento = valores.get(FATURAMENTO);
            if (!faturamento.isEmpty()) {
                Optional<BigDecimal> numeroFaturamento = lerDecimal(faturamento);
                if (numeroFaturamento.isEmpty()) {
                    erros.add(FATURAMENTO + ": '" + faturamento + "' não é um número");
                } else if (numeroFaturamento.get().signum() < 0) {
                    erros.add(FATURAMENTO + ": não pode ser negativo");
                } else if (numeroFaturamento.get().compareTo(LIMITE_FATURAMENTO) >= 0) {
                    erros.add(FATURAMENTO + ": valor acima do permitido");
                } else {
                    if (numeroFaturamento.get().signum() == 0) {
                        avisos.add(FATURAMENTO + ": valor zerado");
                    }
                    valores.put(FATURAMENTO, numeroFaturamento.get().setScale(2, RoundingMode.HALF_UP).toPlainString());
                }
            }

            String servicos = valores.get(SERVICOS);
            if (!servicos.isEmpty() && Arrays.stream(servicos.split(";")).allMatch(String::isBlank)) {
                erros.add(SERVICOS + ": nenhum serviço informado");
            }

            String data = valores.get(DATA);
            if (!data.isEmpty()) {
                Optional<LocalDate> dataContratacao = lerData(data);
                if (dataContratacao.isEmpty() || dataContratacao.get().getYear() < 1990) {
                    erros.add(DATA + ": '" + data + "' não é uma data válida (use dd/mm/aaaa ou aaaa-mm-dd)");
                } else {
                    if (dataContratacao.get().isAfter(hoje)) {
                        avisos.add(DATA + ": data no futuro");
                    }
                    valores.put(DATA, dataContratacao.get().toString());
                }
            }

            String uf = valores.get(UF);
            if (!uf.isEmpty() && !RE_UF.matcher(uf).matches()) {
                erros.add(UF + ": '" + uf + "' inválida (use a sigla com 2 letras)");
            }

            if (!codigo.isEmpty() && !valores.get(NOME).isEmpty()) {
                String chave = valores.get(NOME).toLowerCase() + "|" + valores.get(FATURAMENTO) + "|" + valores.get(DATA);
                Integer anterior = conteudos.putIfAbsent(chave, numero);
                if (anterior != null && !codigo.equalsIgnoreCase(codigoDoConteudo.get(chave))) {
                    avisos.add(NOME + ": possível duplicidade (mesmo nome, faturamento e data da linha " + anterior + ")");
                }
                codigoDoConteudo.putIfAbsent(chave, codigo);
            }

            saida.add(new ResultadoLinha(numero, valores, erros, avisos));
        }

        return saida;
    }

    public List<ClienteDTO> validarEPreparar(List<ClienteDTO> clientes) {
        List<LinhaEnvio> linhas = new ArrayList<>(clientes.size());
        for (int i = 0; i < clientes.size(); i++) {
            ClienteDTO cliente = clientes.get(i);
            linhas.add(new LinhaEnvio(
                    i + 2,
                    cliente.codigoCliente(),
                    cliente.nomeCliente(),
                    cliente.consultor(),
                    cliente.segmentoCliente(),
                    cliente.nivelCliente(),
                    cliente.faturamentoAnual() == null ? null : cliente.faturamentoAnual().toString(),
                    cliente.servicoContratados(),
                    cliente.dataContratacao() == null ? null : cliente.dataContratacao().toString(),
                    cliente.cidade(),
                    cliente.uf()));
        }

        List<ResultadoLinha> resultados = validarLinhas(linhas);
        List<ClienteDTO> preparados = new ArrayList<>(resultados.size());
        for (ResultadoLinha resultado : resultados) {
            if (!resultado.valida()) {
                throw new IllegalArgumentException("Linha " + resultado.numero() + ": " + String.join(" | ", resultado.erros()));
            }
            Map<String, String> valores = resultado.valores();
            preparados.add(new ClienteDTO(
                    valores.get(CODIGO),
                    valores.get(NOME),
                    formatarNome(valores.get(CONSULTOR)),
                    padronizarSegmento(valores.get(SEGMENTO)),
                    valores.get(NIVEL).toUpperCase(),
                    Double.valueOf(valores.get(FATURAMENTO)),
                    valores.get(SERVICOS),
                    LocalDate.parse(valores.get(DATA)),
                    valores.get(CIDADE),
                    valores.get(UF).toUpperCase()));
        }
        return preparados;
    }

    private Map<String, String> mapear(LinhaEnvio linha) {
        Map<String, String> valores = new LinkedHashMap<>();
        valores.put(CODIGO, linha.codigoCliente());
        valores.put(NOME, linha.nomeCliente());
        valores.put(CONSULTOR, linha.consultor());
        valores.put(SEGMENTO, linha.segmento());
        valores.put(NIVEL, linha.nivelCliente());
        valores.put(FATURAMENTO, linha.faturamentoAnual());
        valores.put(SERVICOS, linha.servicosContratados());
        valores.put(DATA, linha.dataContratacao());
        valores.put(CIDADE, linha.cidade());
        valores.put(UF, linha.uf());
        return valores;
    }

    private String padronizarSegmento(String segmento) {
        String normalizado = segmento.strip().toLowerCase();
        return switch (normalizado) {
            case "ind.", "industria", "indústria", "industrial" -> "Indústria";
            case "comercio", "comércio" -> "Comércio";
            case "servicos", "serviços" -> "Serviços";
            case "saude", "saúde" -> "Saúde";
            case "educacao", "educação" -> "Educação";
            case "tecnologia" -> "Tecnologia";
            default -> null;
        };
    }

    private String formatarNome(String nome) {
        String texto = nome.strip().toLowerCase();
        if (texto.isEmpty()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }

    static Optional<BigDecimal> lerDecimal(String texto) {
        String t = texto.replace("R$", "").replace('\u00a0', ' ').replaceAll("\\s", "");
        if (t.isEmpty() || !t.matches("^-?[0-9.,]+$")) {
            return Optional.empty();
        }
        boolean ponto = t.contains(".");
        boolean virgula = t.contains(",");
        if (ponto && virgula) {
            if (t.lastIndexOf(',') > t.lastIndexOf('.')) {
                t = t.replace(".", "").replace(',', '.');
            } else {
                t = t.replace(",", "");
            }
        } else if (virgula) {
            t = t.chars().filter(c -> c == ',').count() > 1 ? t.replace(",", "") : t.replace(',', '.');
        } else if (ponto && t.matches("^-?\\d{1,3}(\\.\\d{3})+$")) {
            t = t.replace(".", "");
        }
        try {
            return Optional.of(new BigDecimal(t));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    static Optional<LocalDate> lerData(String texto) {
        try {
            var iso = DATA_ISO.matcher(texto);
            if (iso.matches()) {
                return Optional.of(LocalDate.parse(iso.group(1)));
            }
            return Optional.of(LocalDate.parse(texto, BR));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}
