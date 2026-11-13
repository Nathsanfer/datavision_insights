"""Entrada do trabalho do Python: processar_lote (chamado pela API em api.py).

O Spring só valida e envia as linhas válidas; o PYTHON cria o lote, limpa (limpeza.py), calculadas (analise.py)
e gravadas no Azure (banco.py).
"""
from __future__ import annotations                                                  # recurso do Python para tipos mais flexíveis

import logging                                                                      # logging: escreve mensagens no terminal

import pandas as pd                                                                 # pandas: tabelas de dados

from analise import calcular                                                        # calcular: gera os indicadores e as frases (analise.py)
from limpeza import tratar                                                          # tratar: limpa e padroniza os dados (limpeza.py)

log = logging.getLogger("analytics")                                                # logger do programa


def processar_lote(engine, arquivo_nome: str, linhas: list[dict],  # processar_lote: cria o lote, trata, calcula e GRAVA tudo no Azure
                   recebidas: int, invalidas: int, com_aviso: int) -> int:          # (continuação) contagens que o backend informou
    import banco                                                                    # banco.py: funções que gravam no banco Azure
    if not linhas:                                                                  # o backend não mandou nenhuma linha?
        raise ValueError("o backend não enviou linhas válidas")                     # erro explicando
    lote_id = banco.criar_lote(engine, arquivo_nome, recebidas, invalidas, len(linhas), com_aviso)  # CRIA o lote no banco (status PROCESSANDO)
    try:                                                                            # tenta processar
        bruto = pd.DataFrame(linhas).astype(object)                                 # lista de dicionários vira tabela do pandas
        res = tratar(bruto)                                                         # limpa os dados
        payload = calcular(res, recebidas)                                          # calcula média, mediana, agrupamentos e frases
        banco.gravar_resultado(engine, lote_id, res.tratado, payload, len(res.rejeitadas))  # GRAVA clientes, contratos e o JSON no Azure
        log.info("Lote %s processado.", lote_id)                                    # registra no terminal
        return lote_id                                                              # deu certo: devolve o id do lote
    except Exception as e:  # noqa: BLE001 - qualquer falha deve aparecer na tela do usuário
        log.exception("Falha no lote %s", lote_id)                                  # escreve o erro completo no terminal
        banco.marcar_erro(engine, lote_id, f"Falha no ETL: {e}")                    # grava ERRO no lote para a tela mostrar
        raise                                                                       # repassa o erro para a API responder 500