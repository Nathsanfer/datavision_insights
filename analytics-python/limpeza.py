"""Transformação: limpeza, padronização e conversão de tipos (etapas 2 a 5 da aba ciencia_de_dados)."""
from __future__ import annotations

import math
import re
import unicodedata
from dataclasses import dataclass, field
from datetime import date, datetime

import pandas as pd

COLUNAS = [
    "codigo_cliente", "nome_cliente", "consultor", "segmento", "nivel_cliente",
    "faturamento_anual", "servicos_contratados", "data_contratacao", "cidade", "uf",
]
OBRIGATORIAS = COLUNAS[:8]

SEGMENTOS = {
    "ind": "Indústria", "industria": "Indústria", "industrial": "Indústria",
    "com": "Comércio", "comercio": "Comércio",
    "serv": "Serviços", "servico": "Serviços", "servicos": "Serviços",
    "saude": "Saúde",
    "edu": "Educação", "educacao": "Educação",
    "tec": "Tecnologia", "ti": "Tecnologia", "tecnologia": "Tecnologia",
}
SERVICOS = {
    "internet dedicada": "Internet Dedicada",
    "link empresarial": "Link Empresarial",
    "firewall": "Firewall",
    "backup": "Backup",
    "mpls": "MPLS",
    "cloud": "Cloud",
}
NIVEIS = {"A", "B", "C"}
PARTICULAS = {"de", "da", "do", "das", "dos", "e"}


def _simples(texto: str) -> str:
    sem_acento = "".join(c for c in unicodedata.normalize("NFKD", str(texto)) if not unicodedata.combining(c))
    return re.sub(r"[^a-z0-9]+", " ", sem_acento.lower()).strip()


def _vazio(v) -> bool:
    if v is None:
        return True
    try:
        return bool(pd.isna(v))
    except (TypeError, ValueError):
        return False


def limpar_texto(v) -> str | None:
    """Remove espaços das pontas e duplicados; vazio vira None."""
    if _vazio(v):
        return None
    t = re.sub(r"\s+", " ", str(v)).strip()
    return t or None


def _aplicar(serie: pd.Series, funcao) -> pd.Series:
    """Como Series.map, mas sempre devolve dtype object e mantém None (o pandas 3 trocaria por NaN)."""
    return pd.Series([funcao(v) for v in serie], index=serie.index, dtype=object)


def titulo(texto: str | None) -> str | None:
    """'ANA SOUZA' -> 'Ana Souza'; mantém 'de', 'da', 'do'... em minúsculo."""
    if texto is None:
        return None
    palavras = texto.lower().split(" ")
    return " ".join(p if (i > 0 and p in PARTICULAS) else p.capitalize() for i, p in enumerate(palavras))


def padronizar_segmento(texto: str | None) -> tuple[str | None, bool]:
    """Devolve (segmento padronizado, reconhecido?). Desconhecido vira Título e fica sinalizado."""
    if texto is None:
        return None, False
    chave = _simples(texto)
    if chave in SEGMENTOS:
        return SEGMENTOS[chave], True
    return titulo(texto), False


def padronizar_servicos(texto: str | None) -> list[str]:
    if texto is None:
        return []
    vistos: list[str] = []
    for parte in texto.split(";"):
        parte = limpar_texto(parte)
        if not parte:
            continue
        nome = SERVICOS.get(_simples(parte)) or titulo(parte)
        if nome not in vistos:
            vistos.append(nome)
    return vistos


def parse_numero(v) -> float | None:
    """Aceita 1850000, '1850000.50', '1.850.000,50' e 'R$ 1.850.000,50'. Inválido -> None."""
    if _vazio(v):
        return None
    if isinstance(v, (int, float)):
        return float(v) if math.isfinite(float(v)) else None
    t = re.sub(r"\s", "", str(v).replace("R$", "").replace(" ", ""))
    if not re.fullmatch(r"-?[0-9.,]+", t or ""):
        return None
    if "." in t and "," in t:
        t = t.replace(".", "").replace(",", ".") if t.rfind(",") > t.rfind(".") else t.replace(",", "")
    elif "," in t:
        t = t.replace(",", "") if t.count(",") > 1 else t.replace(",", ".")
    elif "." in t and re.fullmatch(r"-?\d{1,3}(\.\d{3})+", t):
        t = t.replace(".", "")
    try:
        return float(t)
    except ValueError:
        return None


def parse_data(v) -> date | None:
    if _vazio(v):
        return None
    if isinstance(v, (datetime, pd.Timestamp)):
        return v.date()
    if isinstance(v, date):
        return v
    t = str(v).strip()
    m = re.match(r"^(\d{4})-(\d{2})-(\d{2})(?:[T ].*)?$", t)
    try:
        if m:
            return date(int(m.group(1)), int(m.group(2)), int(m.group(3)))
        return datetime.strptime(t, "%d/%m/%Y").date()
    except ValueError:
        return None


@dataclass
class ResultadoTransformacao:
    tratado: pd.DataFrame
    rejeitadas: pd.DataFrame
    padronizacoes: dict[str, int] = field(default_factory=dict)
    segmentos_originais: list[dict] = field(default_factory=list)
    avisos: list[str] = field(default_factory=list)


def tratar(bruto: pd.DataFrame) -> ResultadoTransformacao:
    """Recebe as linhas como texto e devolve o DataFrame tratado + as linhas que o ETL recusou."""
    df = bruto.copy().reset_index(drop=True)
    df.columns = [re.sub(r"(?<!^)(?=[A-Z])", "_", str(c)).lower() for c in df.columns]
    for c in COLUNAS:
        if c not in df.columns:
            df[c] = None
    if "linha" not in df.columns:
        df["linha"] = df.index + 2
    df = df[["linha"] + COLUNAS].astype(object)

    # Limpeza de espacos.
    # Preserva os valores originais para calcular as padronizacoes.
    brutos = {c: df[c].copy() for c in COLUNAS}
    for c in COLUNAS:
        if c not in ("faturamento_anual", "data_contratacao"):
            df[c] = _aplicar(df[c], limpar_texto)

    # Padronizacao dos valores textuais.
    df["segmento_original"] = df["segmento"]
    padrao = [padronizar_segmento(v) for v in df["segmento"]]
    df["segmento"] = pd.Series([p[0] for p in padrao], index=df.index, dtype=object)
    desconhecidos_idx = {i for i, (v, (_, ok)) in zip(df.index, zip(df["segmento_original"], padrao)) if v is not None and not ok}

    df["codigo_cliente"] = _aplicar(df["codigo_cliente"], lambda x: x.upper() if x else x)
    df["consultor"] = _aplicar(df["consultor"], titulo)
    df["nivel_cliente"] = _aplicar(df["nivel_cliente"], lambda x: x.upper() if x else x)
    df["uf"] = _aplicar(df["uf"], lambda x: x.upper() if x and len(x) == 2 and x.isalpha() else None)
    df["servicos"] = _aplicar(df["servicos_contratados"], padronizar_servicos)

    # Conversao de numeros e datas.
    df["faturamento_anual"] = _aplicar(df["faturamento_anual"], parse_numero)
    df["data_contratacao"] = _aplicar(df["data_contratacao"], parse_data)

    # Validacao defensiva das linhas.
    motivos: list[list[str]] = [[] for _ in range(len(df))]
    for i, row in df.iterrows():
        for c in OBRIGATORIAS:
            vazio = (not row["servicos"]) if c == "servicos_contratados" else _vazio(row[c])
            if vazio:
                motivos[i].append(f"{c}: vazio ou inválido")
        if i in desconhecidos_idx:
            motivos[i].append(f"segmento: '{row['segmento_original']}' não reconhecido")
        if not _vazio(row["nivel_cliente"]) and row["nivel_cliente"] not in NIVEIS:
            motivos[i].append(f"nivel_cliente: '{row['nivel_cliente']}' fora de A/B/C")
        if not _vazio(row["faturamento_anual"]) and row["faturamento_anual"] < 0:
            motivos[i].append("faturamento_anual: negativo")
    duplicado = df["codigo_cliente"].notna() & df["codigo_cliente"].duplicated(keep="first")
    for i in df.index[duplicado]:
        motivos[i].append("codigo_cliente: duplicado")

    recusada = pd.Series([bool(m) for m in motivos], index=df.index)
    rejeitadas = df.loc[recusada, ["linha", "codigo_cliente", "nome_cliente"]].copy()
    rejeitadas["motivo"] = [" | ".join(motivos[i]) for i in df.index[recusada]]

    # Relatorio das alteracoes de padronizacao.
    def mudou(coluna: str, original: pd.Series) -> int:
        pares = zip(original, df[coluna])
        return sum(1 for a, b in pares if not _vazio(a) and str(a) != ("" if _vazio(b) else str(b)))

    padronizacoes = {
        "segmento": mudou("segmento", brutos["segmento"]),
        "consultor": mudou("consultor", brutos["consultor"]),
        "nivel_cliente": mudou("nivel_cliente", brutos["nivel_cliente"]),
    }
    contagem = (
        df[~recusada][["segmento_original", "segmento"]].dropna().value_counts().reset_index(name="ocorrencias")
    )
    segmentos_originais = [
        {"original": str(r.segmento_original), "padronizado": str(r.segmento), "ocorrencias": int(r.ocorrencias)}
        for r in contagem.sort_values(["segmento", "ocorrencias"], ascending=[True, False]).itertuples()
    ]

    avisos: list[str] = []

    tratado = df[~recusada].copy()
    tratado["faturamento_anual"] = tratado["faturamento_anual"].astype(float)
    tratado["qtd_servicos"] = tratado["servicos"].map(len)
    tratado["data_contratacao"] = pd.to_datetime(tratado["data_contratacao"].astype("datetime64[ns]"))
    tratado["mes"] = tratado["data_contratacao"].dt.strftime("%Y-%m")
    tratado = tratado.reset_index(drop=True)[[
        "linha", "codigo_cliente", "nome_cliente", "consultor", "segmento", "segmento_original", "nivel_cliente",
        "faturamento_anual", "servicos", "qtd_servicos", "data_contratacao", "mes", "cidade", "uf",
    ]]
    return ResultadoTransformacao(tratado, rejeitadas.reset_index(drop=True), padronizacoes, segmentos_originais, avisos)