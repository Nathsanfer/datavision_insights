package com.cti.backend_java.dto;

import java.util.List;

public class UploadDtos {
    private UploadDtos() {}

    public record EnvioValidadasRequest(String arquivoNome, Integer descartadas, List<LinhaEnvio> linhas) {}

    public record LinhaEnvio(Integer numeroLinha, String codigoCliente, String nomeCliente, String consultor, String segmento, String nivelCliente, String faturamentoAnual, String servicosContratados, String dataContratacao, String cidade, String uf) {}
}
