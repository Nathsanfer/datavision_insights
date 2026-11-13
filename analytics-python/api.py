# ============================================================================
# api.py — a "porta de entrada" do Python para o DEPLOY: transforma o ETL em uma API (FastAPI).
# O Spring chama POST /etl enviando as linhas válidas; o Python CRIA o lote, limpa, calcula e GRAVA tudo no Azure..
# Como ligar:   uvicorn api:app --port 8000         (documentação automática em http://localhost:8000/docs)
# ============================================================================

# --- BLOCO 1: IMPORTAÇÕES DE BIBLIOTECAS ---
# Importamos ferramentas prontas para registrar logs, ler variáveis de ambiente, criar a API web, 
# manipular tabelas com Pandas e validar os dados que chegam usando Pydantic.
import logging                                            # logging: escreve mensagens no terminal para sabermos o que está acontecendo
import os                                                 # os: lê variáveis de ambiente do sistema (como a senha ETL_API_KEY)
from typing import Any                                    # Any: diz que um valor pode ser de "qualquer tipo" (número, texto, etc.)

from fastapi import FastAPI, Header, HTTPException        # FastAPI: cria a API; Header: lê cabeçalhos da requisição; HTTPException: devolve erros HTTP
import pandas as pd                                       # pandas: biblioteca para criar e manipular tabelas de dados (DataFrames)
from pydantic import BaseModel                            # BaseModel: classe do Pydantic para descrever e validar o formato do JSON enviado pelo Spring

import config                                             # config.py: arquivo nosso que carrega as configurações e senhas do arquivo .env
from analise import calcular                              # calcular: função que gera os indicadores estatísticos e as frases analíticas (analise.py)
from limpeza import tratar                                # tratar: função que limpa e valida os dados brutos (limpeza.py)
from main import processar_lote                           # processar_lote: a MESMA função do modo de estudo que trata, calcula e grava no Azure


# --- BLOCO 2: CONFIGURAÇÃO INICIAL DA APLICAÇÃO ---
# Criamos o "registrador" (logger) para mensagens e instanciamos o FastAPI que vai gerenciar nossas rotas web.
log = logging.getLogger("analytics")                      # Cria o logger específico para acompanhar os eventos da nossa API
app = FastAPI(title="CTI Insights — ETL em Python")       # Instancia o FastAPI dando um título para a documentação automática
_engine = None                                            # Variável global que vai guardar a conexão com o banco Azure (criada só na primeira vez)


# --- BLOCO 3: DEFINIÇÃO DO FORMATO DOS DADOS (Pydantic) ---
# Aqui dizemos ao FastAPI exatamente como deve ser o JSON que o sistema Spring vai nos enviar.
class Pedido(BaseModel):                                  # Pedido: define o formato e o tipo dos dados que chegam na rota /etl
    arquivoNome: str = "planilha"                         # arquivoNome: nome do arquivo que foi processado na ponta (padrão é "planilha")
    recebidas: int                                        # recebidas: total de linhas que a planilha tinha (boas + ruins), para o relatório de qualidade
    invalidas: int = 0                                    # invalidas: quantas linhas continham erros e foram descartadas (padrão 0)
    comAviso: int = 0                                     # comAviso: quantas linhas válidas passaram, mas possuem algum aviso (padrão 0)
    linhas: list[dict[str, Any]]                          # linhas: lista contendo apenas os dados das linhas válidas em formato de dicionário


# --- BLOCO 4: GERENCIAMENTO DA CONEXÃO COM O BANCO ---
# Função auxiliar para garantir que a conexão com o Azure seja aberta apenas uma vez (padrão Singleton simples).
def _engine_azure():                                      # Função que devolve a conexão ativa com o banco Azure
    global _engine                                        # Indica que vamos usar a variável global _engine criada lá em cima
    if _engine is None:                                   # Verifica se a conexão ainda não foi aberta (se está vazia)
        import banco                                      # banco.py: arquivo responsável pelas operações e conexão com o Azure
        _engine = banco.criar_engine()                    # Abre de fato a conexão com o banco e guarda na variável global
    return _engine                                        # Retorna a conexão pronta para uso


# --- BLOCO 5: ROTA DE VERIFICAÇÃO DE SAÚDE (HEALTHCHECK) ---
# Uma rota simples para confirmar se a API está ligada e funcionando.
@app.get("/saude")                                        # Rota GET /saude: usada para o Spring (ou você) conferir se a API está ativa
def saude() -> dict:                                      # Função executada quando alguém acessa esse endereço no navegador
    return {"status": "ok"}                               # Retorna um JSON simples confirmando que está tudo certo


# --- BLOCO 6: ROTA PRINCIPAL DO ETL (GRAVA NO AZURE) ---
# Recebe os dados validados do Spring, valida a chave de segurança e executa o processamento completo salvando no banco.
@app.post("/etl")                                         # Rota POST /etl: recebe o lote enviado pelo Spring, processa e grava no Azure
def etl(pedido: Pedido, x_etl_key: str | None = Header(default=None)) -> dict:      # Recebe o corpo validado e o cabeçalho X-ETL-Key de segurança
    # Passo 6.1: Validação da senha/chave de acesso
    chave = os.getenv("ETL_API_KEY", "").strip()          # Lê a senha configurada no servidor (variável de ambiente ETL_API_KEY)
    if chave and x_etl_key != chave:                      # Se houver senha configurada e a chave enviada no cabeçalho for diferente...
        raise HTTPException(status_code=401, detail="Chave do ETL inválida.")      # Rejeita a requisição retornando o Erro 401 (Não autorizado)
    
    # Passo 6.2: Verificação se há dados válidos
    if not pedido.linhas:                                 # Se a lista de linhas válidas veio vazia...
        raise HTTPException(status_code=422, detail="Nenhuma linha válida foi enviada.")      # Rejeita retornando Erro 422 (Dados impróprios/não processáveis)
    
    # Passo 6.3: Execução do processo de ETL e gravação
    try:                                                  # Bloco protegido para capturar qualquer erro inesperado durante o processamento
        lote_id = processar_lote(_engine_azure(), pedido.arquivoNome, pedido.linhas,  # Chama a função central que limpa, calcula e grava no Azure
                       pedido.recebidas, pedido.invalidas, pedido.comAviso)         # Passa também os contadores gerais do lote
    except Exception as e:  # noqa: BLE001                # Se acontecer qualquer erro no meio do caminho...
        raise HTTPException(status_code=500, detail=f"Falha no ETL: {e}")           # Devolve Erro 500 (Erro interno do servidor) com a mensagem do erro
    
    # Passo 6.4: Resposta de sucesso
    return {"ok": True, "loteId": lote_id}                                   # Retorna HTTP 200 indicando sucesso (o Python não devolve os dados, pois já gravou tudo no banco)


# --- BLOCO 7: ROTA DE TESTE RÁPIDO (SEM BANCO) ---
# Permite testar a limpeza e os cálculos estatísticos enviando um JSON direto, sem persistir nada no Azure.
@app.post("/testar")                                      # Rota POST /testar: simula o processamento sem mexer no banco de dados do Azure
def testar(linhas: list[dict[str, Any]]) -> dict:         # Recebe apenas uma lista simples de dicionários (como o arquivo clientes.json)
    res = tratar(pd.DataFrame(linhas).astype(object))     # Converte a lista para uma tabela Pandas e executa a função de limpeza (tratar)
    return calcular(res, len(linhas))                     # Executa os cálculos estatísticos (calcular) e devolve os números e frases prontas