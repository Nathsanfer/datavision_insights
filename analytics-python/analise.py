"""Análise: calcula as estatísticas do dashboard: média, mediana, moda, desvio padrão, quartis e correlação.

Devolve o objeto `Estatisticas` do contrato (docs/CONTRATO.md): camelCase, 100% serializável em JSON
(NaN e infinito viram null)."""
from __future__ import annotations                                                  # recurso do Python para tipos mais flexíveis

import math                                                                         # math: funções matemáticas
from datetime import datetime, timezone                                             # datetime e timezone: data e hora em UTC

import pandas as pd                                                                 # pandas: tabelas de dados
from scipy import stats as sp                                                       # scipy.stats: testes estatísticos (correlação)

from insights import gerar_insights                                                 # gerar_insights: monta as frases (insights.py)
from limpeza import ResultadoTransformacao                                          # o resultado da limpeza (limpeza.py)


# ------------------------------------------------------------------ utilitários
def num(x, casas: int | None = 2):                                                  # num: converte para número seguro para JSON
    """Número Python seguro para JSON (NaN/inf viram None)."""
    if x is None:                                                                   # sem valor
        return None                                                                 # devolve None
    try:                                                                            # tenta converter
        f = float(x)                                                                # vira float
    except (TypeError, ValueError):                                                 # se não der
        return None                                                                 # devolve None
    if not math.isfinite(f):                                                        # NaN ou infinito não existem em JSON
        return None                                                                 # devolve None
    return round(f, casas) if casas is not None else f                              # arredonda para 2 casas


def _pct(parte, todo) -> float:                                                     # _pct: percentual (parte em relação ao todo)
    return round(100 * parte / todo, 1) if todo else 0.0                            # evita dividir por zero


def moda(serie: pd.Series):                                                         # moda: o valor que mais se repete
    m = serie.dropna().mode()                                                       # conta os valores (sem vazios) e pega o mais frequente
    return None if m.empty else m.iloc[0]                                           # devolve o primeiro, ou None se não houver


def _resumo_serie(serie: pd.Series) -> dict:                                        # _resumo_serie: calcula as medidas de uma coluna de números
    n = int(serie.size)                                                             # quantidade de itens
    return {                                                                        # monta o dicionário de resultados
        "clientes": n,                                                              # quantidade de clientes
        "total": num(serie.sum()),                                                  # soma
        "media": num(serie.mean()),                                                 # MÉDIA
        "mediana": num(serie.median()),                                             # MEDIANA (valor do meio)
        "desvioPadrao": num(serie.std(ddof=1)) if n > 1 else None,                  # desvio padrão (precisa de 2 ou mais itens)
        "minimo": num(serie.min()),                                                 # menor valor
        "q1": num(serie.quantile(0.25)),                                            # 1º quartil (25% dos valores ficam abaixo)
        "q3": num(serie.quantile(0.75)),                                            # 3º quartil (75%)
        "maximo": num(serie.max()),                                                 # maior valor
    }                                                                               # fim do dicionário


# ---------------------------------------------------------------------- blocos
def _agrupar(df: pd.DataFrame, coluna: str, nome: str) -> list[dict]:               # _agrupar: calcula tudo por categoria (segmento, nível ou consultor)
    """Uma linha por categoria, do maior para o menor número de clientes."""
    saida = []                                                                      # lista de resultados
    total_clientes, total_fat = len(df), df["faturamento_anual"].sum()              # total de clientes e faturamento total
    for chave, g in df.groupby(coluna, sort=False):                                 # para cada grupo da categoria
        r = _resumo_serie(g["faturamento_anual"])                                   # medidas do faturamento desse grupo
        saida.append({                                                              # acrescenta um dicionário à lista
            nome: str(chave), "clientes": r["clientes"], "percentual": _pct(len(g), total_clientes),  # nome do grupo, clientes e % de clientes
            "percentualFaturamento": _pct(g["faturamento_anual"].sum(), total_fat),  # % do faturamento total
            "faturamentoTotal": r["total"], "total": r["total"], "media": r["media"], "mediana": r["mediana"],  # faturamento total, média e mediana
            "desvioPadrao": r["desvioPadrao"], "minimo": r["minimo"], "maximo": r["maximo"],  # desvio padrão, mínimo e máximo
        })                                                                          # fim do dicionário
    return sorted(saida, key=lambda x: (-x["clientes"], x[nome]))                   # ordena: mais clientes primeiro


def _por_mes(df: pd.DataFrame) -> list[dict]:                                       # _por_mes: quantos clientes contrataram em cada mês
    """Meses contínuos, inclusive os sem contratação."""
    if df.empty:                                                                    # tabela vazia
        return []                                                                   # devolve lista vazia
    por_mes = df.groupby("mes").agg(clientes=("codigo_cliente", "count"), faturamento=("faturamento_anual", "sum"))  # agrupa por mês e conta clientes
    meses = pd.period_range(df["data_contratacao"].min(), df["data_contratacao"].max(), freq="M").strftime("%Y-%m")  # todos os meses entre o primeiro e o último
    por_mes = por_mes.reindex(meses, fill_value=0)                                  # meses sem contratação ficam com zero
    acumulado = por_mes["clientes"].cumsum()                                        # soma acumulada
    return [{"mes": m, "clientes": int(r.clientes), "faturamento": num(r.faturamento), "acumulado": int(acumulado[m])}  # uma linha por mês
            for m, r in por_mes.iterrows()]                                         # (continuação) percorre os meses


def _por_servico(df: pd.DataFrame) -> list[dict]:                                   # _por_servico: quantos clientes contrataram cada serviço
    longo = df[["codigo_cliente", "servicos", "faturamento_anual"]].explode("servicos").dropna(subset=["servicos"])  # uma linha por (cliente, serviço)
    total = len(df)                                                                 # total de clientes
    saida = [{"servico": str(nome), "contratos": int(g["codigo_cliente"].nunique()),  # monta uma linha por serviço
              "percentual": _pct(g["codigo_cliente"].nunique(), total),             # percentual de clientes
              "faturamentoMedio": num(g["faturamento_anual"].mean())}               # faturamento médio de quem contratou
             for nome, g in longo.groupby("servicos")]                              # para cada serviço
    return sorted(saida, key=lambda x: (-x["contratos"], x["servico"]))             # ordena: mais contratado primeiro


def _correlacao(df: pd.DataFrame) -> dict:                                          # _correlacao: relação entre nº de serviços e faturamento
    x, y = df["qtd_servicos"].astype(float), df["faturamento_anual"].astype(float)  # x = nº de serviços; y = faturamento
    base = {"pearson": None, "p": None, "n": int(len(df)), "spearman": None, "spearmanP": None, "interpretacao": None}  # resultado padrão (sem valores)
    if len(df) < 3 or x.nunique() < 2 or y.nunique() < 2:                           # poucos dados: não dá para calcular
        return base                                                                 # devolve o padrão
    r, p = sp.pearsonr(x, y)                                                        # correlação de Pearson (r e p)
    rho, p_rho = sp.spearmanr(x, y)                                                 # correlação de Spearman
    forca = "forte" if abs(r) >= 0.7 else "moderada" if abs(r) >= 0.4 else "fraca"  # força: forte, moderada ou fraca
    base.update({"pearson": num(r, 3), "p": num(p, 6), "spearman": num(rho, 3), "spearmanP": num(p_rho, 6),  # guarda os números
                 "interpretacao": f"Correlação {forca} {'positiva' if r > 0 else 'negativa'} entre a quantidade de serviços e o faturamento."})  # e a frase explicando
    return base                                                                     # devolve o resultado


# ---------------------------------------------------------------------- função principal
def calcular(res: ResultadoTransformacao, linhas_recebidas: int) -> dict:           # calcular: função principal; recebe a limpeza e devolve o JSON
    df = res.tratado                                                                # tabela dos clientes tratados
    if df.empty:                                                                    # sem clientes
        raise ValueError("Não há clientes válidos para calcular estatísticas.")     # erro explicando
    fat = df["faturamento_anual"]                                                   # coluna de faturamento
    r = _resumo_serie(fat)                                                          # medidas gerais (média, mediana...)

    por_segmento = _agrupar(df, "segmento", "segmento")                             # por segmento
    por_nivel = _agrupar(df.sort_values("nivel_cliente"), "nivel_cliente", "nivel")  # por nível
    por_nivel.sort(key=lambda x: x["nivel"])  # A -> C
    por_consultor = _agrupar(df, "consultor", "consultor")                          # por consultor
    por_servico = _por_servico(df)                                                  # por serviço
    por_mes = _por_mes(df)                                                          # por mês
    correlacao = _correlacao(df)                                                    # correlação

    resumo = {                                                                      # resumo geral (cartões do dashboard)
        "totalClientes": r["clientes"], "faturamentoTotal": r["total"], "media": r["media"], "mediana": r["mediana"],  # total de clientes, faturamento, média e mediana
        "desvioPadrao": r["desvioPadrao"], "minimo": r["minimo"], "maximo": r["maximo"], "q1": r["q1"], "q3": r["q3"],  # desvio padrão, mínimo, máximo e quartis
        "segmentoMaisFrequente": moda(df["segmento"]), "nivelMaisFrequente": moda(df["nivel_cliente"]),  # segmento e nível mais frequentes (moda)
        "servicoMaisContratado": moda(df["servicos"].explode()),                    # serviço mais contratado (moda)
        "mediaServicosPorCliente": num(df["qtd_servicos"].mean()),                  # média de serviços por cliente
        "coeficienteVariacao": num(fat.std(ddof=1) / fat.mean(), 3) if len(fat) > 1 and fat.mean() else None,  # coeficiente de variação (desvio / média)
        "consultores": int(df["consultor"].nunique()),                              # número de consultores
    }                                                                               # fim do resumo
    top = df.sort_values("faturamento_anual", ascending=False).head(5)              # os 5 maiores clientes

    return {                                                                        # devolve o dicionário que vira JSON no banco
        "geradoEm": datetime.now(timezone.utc).isoformat(timespec="seconds"),       # quando foi gerado
        "resumo": resumo,                                                           # resumo geral
        "porSegmento": por_segmento,                                                # clientes por segmento
        "porNivel": por_nivel,                                                      # clientes por nível
        "porConsultor": por_consultor,                                              # faturamento por consultor
        "porServico": por_servico,                                                  # serviços
        "porMes": por_mes,                                                          # contratações por mês
        "correlacao": correlacao,                                                   # correlação
        "topClientes": [{"codigo": x.codigo_cliente, "nome": x.nome_cliente, "segmento": x.segmento,  # 5 maiores clientes
                         "nivel": x.nivel_cliente, "faturamento": num(x.faturamento_anual)} for x in top.itertuples()],  # (continuação)
        "qualidade": {                                                              # relatório de qualidade da planilha
            "linhasRecebidas": int(linhas_recebidas),                               # linhas recebidas
            "linhasValidas": int(len(df)),                                          # linhas válidas
            "linhasInvalidas": int(linhas_recebidas - len(df)),                     # linhas inválidas
            "correcoesSegmento": res.padronizacoes["segmento"],                     # correções de segmento
            "correcoesConsultor": res.padronizacoes["consultor"],                   # correções de consultor
            "correcoesNivel": res.padronizacoes["nivel_cliente"],                   # correções de nível
            "segmentosOriginais": res.segmentos_originais,                          # segmentos que foram corrigidos
            "avisos": res.avisos,                                                   # avisos
        },                                                                          # fim da qualidade
        "insights": gerar_insights(resumo, por_segmento, por_nivel, por_consultor, por_servico, por_mes, correlacao),  # frases de insights
    }                                                                               # fim do dicionário