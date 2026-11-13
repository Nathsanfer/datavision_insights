"""Banco: GRAVA no PostgreSQL (Azure) os clientes, contratos e o JSON do dashboard. As linhas chegam do backend."""

# --- BLOCO 1: IMPORTAÇÕES DE BIBLIOTECAS ---
# Importamos ferramentas para manipulação de textos JSON, registro de logs, datas e manipulação de banco de dados SQL.
from __future__ import annotations                                    # Recurso do Python para aceitar tipos mais flexíveis nas dicas de código
import json                                                           # json: converte dicionários do Python em formato de texto JSON (e vice-versa)
import logging                                                        # logging: escreve mensagens e alertas no terminal do sistema
from datetime import datetime, timezone                               # datetime e timezone: manipulam data e hora no padrão UTC (sem fuso horário)

import pandas as pd                                                   # pandas: biblioteca usada para lidar com tabelas de dados (DataFrames)
from sqlalchemy import create_engine, text                            # SQLAlchemy: create_engine abre a conexão com o banco; text executa comandos SQL
from sqlalchemy.engine import Connection, Engine                      # Tipos especiais do SQLAlchemy usados apenas para indicar os tipos dos dados

from config import database_url                                       # config: carrega a função que busca o endereço secreto do banco no arquivo .env

log = logging.getLogger("analytics")                                  # Cria o registrador de logs específico para acompanhar o salvamento no banco


# --- BLOCO 2: CONEXÃO E GERENCIAMENTO DE DATA/HORA ---
# Funções utilitárias para abrir a porta de comunicação com o Azure e padronizar o horário UTC.
DDL_ETL = [
    "CREATE SCHEMA IF NOT EXISTS etl",
    "CREATE TABLE IF NOT EXISTS etl.upload_lote (id BIGSERIAL PRIMARY KEY, arquivo_nome VARCHAR(255), enviado_em TIMESTAMP, status VARCHAR(20), total_linhas INT, linhas_validas INT, linhas_invalidas INT, linhas_com_aviso INT, mensagem TEXT)",
    "CREATE TABLE IF NOT EXISTS etl.consultor (id BIGSERIAL PRIMARY KEY, nome VARCHAR(120) UNIQUE NOT NULL)",
    "CREATE TABLE IF NOT EXISTS etl.segmento (id BIGSERIAL PRIMARY KEY, nome VARCHAR(80) UNIQUE NOT NULL)",
    "CREATE TABLE IF NOT EXISTS etl.servico (id BIGSERIAL PRIMARY KEY, nome VARCHAR(120) UNIQUE NOT NULL)",
    "CREATE TABLE IF NOT EXISTS etl.cliente (id BIGSERIAL PRIMARY KEY, lote_id BIGINT REFERENCES etl.upload_lote(id) ON DELETE CASCADE, codigo_cti VARCHAR(60), nome VARCHAR(255), consultor_id BIGINT REFERENCES etl.consultor(id), segmento_id BIGINT REFERENCES etl.segmento(id), nivel VARCHAR(20), faturamento_anual NUMERIC(18,2), cidade VARCHAR(120), uf VARCHAR(10))",
    "CREATE TABLE IF NOT EXISTS etl.contrato (id BIGSERIAL PRIMARY KEY, cliente_id BIGINT REFERENCES etl.cliente(id) ON DELETE CASCADE, servico_id BIGINT REFERENCES etl.servico(id), data_contratacao DATE)",
    "CREATE TABLE IF NOT EXISTS etl.estatistica_lote (lote_id BIGINT PRIMARY KEY REFERENCES etl.upload_lote(id) ON DELETE CASCADE, gerado_em TIMESTAMP, resultado JSONB)",
]


def criar_engine() -> Engine:                                         # criar_engine: abre e retorna a "porta" de conexão com o banco PostgreSQL no Azure
    # As tabelas do ETL ficam no schema próprio "etl", para não colidir com as tabelas do Spring (cliente, consultor...)
    engine = create_engine(database_url(), pool_pre_ping=True, connect_args={"options": "-csearch_path=etl"})
    with engine.begin() as conn:
        for ddl in DDL_ETL:
            conn.execute(text(ddl))
    return engine

def agora_utc() -> datetime:                                          # agora_utc: pega a data e hora atual no padrão UTC
    """O Spring Boot grava TIMESTAMP em UTC (sem fuso); fazemos o mesmo."""
    return datetime.now(timezone.utc).replace(tzinfo=None)            # Remove a informação de fuso horário para o PostgreSQL salvar sem conflitos


# --- BLOCO 3: CRIAÇÃO E CONTROLE DO LOTE DE UPLOAD ---
# Funções para registrar na tabela 'upload_lote' o início do processamento ou marcar erros caso algo falhe.
def criar_lote(engine: Engine, arquivo_nome: str, recebidas: int,       # criar_lote: o PYTHON cria o lote (o Java envia, mas o Python registra o andamento)
               invalidas: int, validas: int, com_aviso: int) -> int:    # Recebe os números de linhas totais, válidas, com aviso e inválidas
    """Cria a linha em upload_lote com status PROCESSANDO e devolve o id novo."""
    with engine.begin() as conn:                                      # Abre uma transação (salva tudo de vez ou cancela se der erro ao sair do bloco)
        return conn.execute(                                          # Executa o comando SQL de inserção e já pede o ID gerado para este lote
            text("INSERT INTO upload_lote (arquivo_nome, enviado_em, status, total_linhas, "  # Colunas da tabela de lotes
                 "linhas_validas, linhas_invalidas, linhas_com_aviso, mensagem) "             # (continuação das colunas)
                 "VALUES (:a, :e, 'PROCESSANDO', :t, :v, :i, :c, 'ETL em andamento...') RETURNING id"),  # Define status inicial como PROCESSANDO
            {"a": arquivo_nome[:255], "e": agora_utc(), "t": recebidas,   # Passa os valores: nome do arquivo, hora UTC e total recebido
             "v": validas, "i": invalidas, "c": com_aviso}              # Passa as contagens e retorna o ID único criado
        ).scalar_one()                                                # Pega apenas o número do ID gerado pelo banco e o devolve

def marcar_erro(engine: Engine, lote_id: int, mensagem: str) -> None:         # marcar_erro: atualiza o status do lote para 'ERRO' se algo der errado
    with engine.begin() as conn:                                      # Abre uma transação no banco de dados
        conn.execute(text("UPDATE upload_lote SET status = 'ERRO', mensagem = :m WHERE id = :id"),  # Altera o status para ERRO e salva o motivo
                     {"id": lote_id, "m": mensagem[:480]})              # Limita a mensagem de erro a 480 caracteres para caber na coluna


# --- BLOCO 4: GRAVAÇÃO DOS DADOS TRATADOS (CARGA COMPLETA) ---
# Limpa dados antigos do lote, gerencia chaves estrangeiras com cache e grava clientes, contratos e estatísticas em JSON.
def gravar_resultado(engine: Engine, lote_id: int, tratado: pd.DataFrame, payload: dict, rejeitadas_etl: int) -> None:  # gravar_resultado: salva tudo no Azure
    """Grava tudo em uma única transação: ou o lote inteiro é processado, ou nada muda."""
    with engine.begin() as conn:                                      # Uma transação só: ou grava tudo com sucesso ou desfaz tudo se houver falha
        # Passo 4.1: Limpeza prévia para evitar duplicidade em caso de reprocessamento do mesmo lote
        conn.execute(text("DELETE FROM cliente WHERE lote_id = :l"), {"l": lote_id})       # Apaga clientes antigos do lote (contratos somem em cascata)
        conn.execute(text("DELETE FROM estatistica_lote WHERE lote_id = :l"), {"l": lote_id})  # Apaga estatísticas antigas vinculadas a este lote

        # Dicionários de cache para evitar consultas repetidas ao banco e ganhar desempenho
        consultores: dict[str, int] = {}                              # Cache: guarda o nome do consultor e o ID correspondente no banco
        segmentos: dict[str, int] = {}                                # Cache: guarda o nome do segmento e o ID correspondente no banco
        servicos: dict[str, int] = {}                                 # Cache: guarda o nome do serviço e o ID correspondente no banco

        # Função auxiliar interna para buscar ou cadastrar consultores, segmentos e serviços
        def id_de(tabela: str, nome: str, cache: dict[str, int]) -> int:            # id_de: encontra ou cria um item e devolve seu ID numérico
            if nome not in cache:                                     # Se o nome ainda não estiver guardado no cache...
                conn.execute(text(f"INSERT INTO {tabela} (nome) VALUES (:n) ON CONFLICT (nome) DO NOTHING"), {"n": nome})  # Insere no banco (ignora se já existir)
                cache[nome] = conn.execute(text(f"SELECT id FROM {tabela} WHERE nome = :n"), {"n": nome}).scalar_one()   # Busca o ID gerado e guarda no cache
            return cache[nome]                                        # Retorna o ID correto para ser usado na tabela principal

        # Passo 4.2: Inserção dos dados de cada cliente limpo
        for r in tratado.itertuples(index=False):                     # Percorre linha por linha da tabela de dados limpos (DataFrame)
            cliente_id = conn.execute(text("""
                INSERT INTO cliente (lote_id, codigo_cti, nome, consultor_id, segmento_id, nivel, faturamento_anual, cidade, uf)
                VALUES (:lote, :codigo, :nome, :consultor, :segmento, :nivel, :fat, :cidade, :uf)
                RETURNING id
            """), {
                "lote": lote_id, "codigo": r.codigo_cliente[:60], "nome": r.nome_cliente[:255],  # Código e nome limitados ao tamanho máximo das colunas
                "consultor": id_de("consultor", r.consultor[:120], consultores),    # Converte o nome do consultor em ID numérico
                "segmento": id_de("segmento", r.segmento[:40], segmentos),          # Converte o nome do segmento em ID numérico
                "nivel": r.nivel_cliente, "fat": round(float(r.faturamento_anual), 2),  # Nível do cliente e faturamento arredondado com 2 casas decimais
                "cidade": r.cidade[:120] if isinstance(r.cidade, str) else None,    # Salva a cidade se for texto válido, senão salva como nulo (None)
                "uf": r.uf if isinstance(r.uf, str) else None,                  # Salva a UF se for texto válido, senão salva como nulo (None)
            }).scalar_one()                                           # Fim do comando de inserção do cliente; captura o ID gerado

            # Passo 4.3: Inserção dos contratos associados ao cliente
            for nome_servico in r.servicos:                           # Para cada serviço que o cliente contratado possui...
                conn.execute(text("""
                    INSERT INTO contrato (cliente_id, servico_id, data_contratacao) VALUES (:c, :s, :d)
                """), {"c": cliente_id, "s": id_de("servico", nome_servico[:120], servicos),  # Vincula o ID do cliente e o ID do serviço
                       "d": r.data_contratacao.date()})               # Salva apenas a data da contratação (sem horário)

        # Passo 4.4: Salvamento das estatísticas consolidadas em formato JSON
        conn.execute(text("INSERT INTO estatistica_lote (lote_id, gerado_em, resultado) VALUES (:l, :g, :r)"),  # Grava o resumo analítico completo
                     {"l": lote_id, "g": agora_utc(), "r": json.dumps(payload, ensure_ascii=False)})  # Converte o dicionário de payload em texto JSON sem corromper acentos

        # Passo 4.5: Atualização final do status do lote para sucesso
        mensagem = f"ETL concluído: {len(tratado)} clientes tratados"  # Mensagem base de sucesso com a quantidade de clientes processados
        if rejeitadas_etl:                                            # Se houve alguma linha rejeitada durante a etapa de limpeza/ETL...
            mensagem += f", {rejeitadas_etl} linhas recusadas na limpeza"  # Adiciona a quantidade de linhas rejeitadas no texto
        conn.execute(text("UPDATE upload_lote SET status = 'PROCESSADO', mensagem = :m WHERE id = :id"),  # Atualiza o status do lote para PROCESSADO
                     {"id": lote_id, "m": mensagem + "."})            # Salva a mensagem final detalhada no banco

    # Passo 4.6: Registro final no terminal do servidor
    log.info("Lote %s gravado: %s clientes, %s consultores, %s segmentos, %s serviços",          # Escreve um log de sucesso informando as contagens finais
             lote_id, len(tratado), len(consultores), len(segmentos), len(servicos))            # Mostra quantos registros foram salvos de cada tipo