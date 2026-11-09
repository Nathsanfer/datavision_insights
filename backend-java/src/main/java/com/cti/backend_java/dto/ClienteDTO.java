package com.cti.backend_java.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record ClienteDTO (

    @NotBlank(message = "O código do cliente não pode estar em branco.")
    String codigoCliente,

    @NotBlank (message = "O nome do cliente não pode estar em branco.")
    String nomeCliente,

    @NotBlank (message = "O consultor é obrigatório.")
    String consultor,

    @NotBlank (message = "O segmento do cliente é obrigatório.")
    String segmentoCliente,

    @NotBlank (message = "O nível do cliente é obrigatório.")
    @Pattern(regexp = "[ABC]", message = "O nível do cliente deve ser A, B ou C.")
    String nivelCliente,

    @NotNull (message = "O faturamneto anual é obrigatório.")
    @Positive(message = "O faturamento anual deve ser maior que zero.")
    Double faturamentoAnual,

    @NotBlank (message = "Informe o serviço contratado pelo cliente.")
    String servicoContratados,

    @NotNull (message = "A data da contratação é obrigatória.")
    LocalDate dataContratacao,

    String cidade,

    @Pattern (regexp = "^$|^[A-Za-z]{2}$", message = "A UF deve possuir duas letras")
    String uf

){}
