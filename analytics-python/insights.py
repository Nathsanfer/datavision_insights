"""Insights: transforma os números da análise em frases em português."""
from __future__ import annotations                                                  # recurso do Python para tipos mais flexíveis


def brl(valor) -> str:                                                              # brl: formata como dinheiro brasileiro
    if valor is None:                                                               # sem valor
        return "—"                                                                  # devolve um traço
    s = f"{valor:,.2f}".replace(",", "X").replace(".", ",").replace("X", ".")       # troca vírgula e ponto: 1,234.50 vira 1.234,50
    return f"R$ {s}"                                                                # devolve com "R$"


def _virg(x) -> str:                                                                # _virg: troca o ponto decimal por vírgula
    return str(x).replace(".", ",")                                                 # 25.0 vira "25,0"


def gerar_insights(resumo, por_segmento, por_nivel, por_consultor, por_servico, por_mes, correlacao) -> list[str]:  # gerar_insights: monta frases em português com os achados
    ins: list[str] = []                                                             # lista de frases
    top = por_segmento[0]                                                           # segmento com mais clientes
    ins.append(f"O segmento {top['segmento']} concentra a maior carteira: {top['clientes']} clientes ({_virg(top['percentual'])}%).")  # frase do segmento

    if resumo["media"] and resumo["mediana"] and resumo["media"] > resumo["mediana"] * 1.1:  # média bem maior que a mediana?
        ins.append(f"A média de faturamento ({brl(resumo['media'])}) é maior que a mediana ({brl(resumo['mediana'])}): "  # frase explicando
                   "poucos clientes muito grandes puxam a média para cima, então a mediana descreve melhor o cliente típico.")  # (continuação) poucos clientes grandes puxam a média
    elif resumo["media"] and resumo["mediana"]:                                     # senão, se são parecidas
        ins.append(f"Média ({brl(resumo['media'])}) e mediana ({brl(resumo['mediana'])}) são próximas: a carteira é equilibrada.")  # frase de equilíbrio

    a = next((n for n in por_nivel if n["nivel"] == "A"), None)                     # procura o nível A
    if a:                                                                           # se existe
        ins.append(f"Os {a['clientes']} clientes nível A representam {_virg(a['percentualFaturamento'])}% do faturamento total, "  # frase do nível A
                   f"com apenas {_virg(a['percentual'])}% dos clientes.")           # (continuação)

    maior = max(por_consultor, key=lambda c: c["faturamentoTotal"])                 # consultor com maior faturamento
    ins.append(f"{maior['consultor']} é o consultor com maior faturamento sob gestão ({brl(maior['faturamentoTotal'])}, {maior['clientes']} clientes).")  # frase do consultor

    if por_servico:                                                                 # se há serviços
        s = por_servico[0]                                                          # o mais contratado
        ins.append(f"{s['servico']} é o serviço mais contratado: presente em {s['contratos']} clientes ({_virg(s['percentual'])}%).")  # frase do serviço

    if por_mes:                                                                     # se há meses
        pico = max(e["clientes"] for e in por_mes)                                  # maior número de contratações em um mês
        meses = [f"{e['mes'][5:]}/{e['mes'][:4]}" for e in por_mes if e["clientes"] == pico]  # meses em que isso aconteceu
        if len(meses) <= 3:                                                         # poucos meses de pico
            ins.append(f"O pico de contratações foi em {', '.join(meses)} ({pico} por mês).")  # frase com os meses
        else:                                                                       # muitos meses de pico
            ins.append(f"As contratações ficaram distribuídas ao longo do tempo: no máximo {pico} por mês, repetido em {len(meses)} meses.")  # frase de distribuição

    if correlacao["pearson"] is not None:                                           # se a correlação foi calculada
        ins.append(correlacao["interpretacao"].rstrip(".") + f" (r de Pearson = {_virg(correlacao['pearson'])}).")  # frase da correlação
    return ins                                                                      # devolve as frases
