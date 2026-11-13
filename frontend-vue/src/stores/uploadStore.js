import { defineStore } from "pinia";
import * as XLSX from "xlsx";
import api from "../services/api";

export const useUploadStore = defineStore("upload", {
  // Armazena a quantidade de dados
  state: () => ({
    arquivo: null,
    dadosOriginais: [],
    dadosTratados: [],
    erros: [],
    carregamento: false,
    enviando: false,
    loteId: null,
    resultado: null,
  }),

  // Calcula a quantidade de dados
  getters: {
    totalClientes: (state) => state.dadosTratados.length,
    totalErros: (state) => state.erros.length,
    clientesNivelA: (state) =>
      state.dadosTratados.filter((cliente) => cliente.nivel_cliente === "A")
        .length,
    inconsistencias: (state) =>
      state.dadosTratados.reduce((linhasComErro, cliente, index) => {
        const motivos = [];
        const nivel = String(cliente.nivel_cliente || "").trim().toUpperCase();
        const faturamentoTexto = String(cliente.faturamento_anual ?? "").trim();
        const faturamento = Number(
          faturamentoTexto.replace(/\./g, "").replace(",", ".")
        );

        if (!["A", "B", "C"].includes(nivel)) {
          motivos.push("Nível deve ser A, B ou C");
        }

        if (!faturamentoTexto || !Number.isFinite(faturamento) || faturamento <= 0) {
          motivos.push("Faturamento não pode ser vazio e deve ser maior que zero");
        }

        if (!String(cliente.consultor || "").trim()) {
          motivos.push("Consultor vazio");
        }

        if (
          !String(cliente.nome_cliente || "").trim() ||
          cliente.nome_cliente === "Sem nome"
        ) {
          motivos.push("Nome da empresa vazio");
        }

        if (!String(cliente.segmento || "").trim() || cliente.segmento === "-") {
          motivos.push("Segmento ou serviço vazio");
        }

        if (motivos.length) {
          linhasComErro.push({
            ...cliente,
            linha: index + 2,
            motivos: motivos.join("; "),
          });
        }

        return linhasComErro;
      }, []),
    temDados: (state) => state.dadosTratados.length > 0,
  },

  // Ações para manipular os dados
  actions: {
    selecionarArquivo(file) {
      this.arquivo = file;
      this.erros = [];
    },

    validarArquivo() {
      if (!this.arquivo) {
        this.erros.push("Selecione uma planilha.");
        return false;
      }

      const nome = this.arquivo.name.toLowerCase();
      const valido =
        nome.endsWith(".xlsx") ||
        nome.endsWith(".xls") ||
        nome.endsWith(".csv");

      if (!valido) this.erros.push("Formato inválido.");
      return valido;
    },

    normalizarSegmento(valor) {
      const raw = String(valor ?? "").trim();
      if (!raw) return "-";

      const semAcento = raw
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase();

      const compactado = semAcento.replace(/[^a-z]/g, "");

      const mapaSegmentos = {
        ind: "Indústria",
        industria: "Indústria",
        comercio: "Comércio",
        servicos: "Serviços",
        servico: "Serviços",
        educacao: "Educação",
        saude: "Saúde",
        tecnologia: "Tecnologia"
      };

      return mapaSegmentos[compactado] || raw;
    },

    async processarPlanilha() {
      if (!this.validarArquivo()) return;

      this.carregamento = true;

      const buffer = await this.arquivo.arrayBuffer();
      const workbook = XLSX.read(buffer, { type: "array" });
      const primeiraAba = workbook.SheetNames[0];
      const planilha = workbook.Sheets[primeiraAba];

      const linhas = XLSX.utils.sheet_to_json(planilha, { defval: "" });

      this.dadosOriginais = linhas;
      this.dadosTratados = linhas.map((linha) => this.tratarLinha(linha));
      this.carregamento = false;
    },

    tratarLinha(linha) {
      const segmentoNormalizado = this.normalizarSegmento(linha.segmento);

      return {
        ...linha,
        consultor: String(linha.consultor || "").trim(),
        segmento: segmentoNormalizado,
        nivel_cliente: String(linha.nivel_cliente || linha.nivel || "").trim().toUpperCase() || "N/A",
      };
    },

    // Converte data (Date do Excel, serial ou texto) para aaaa-mm-dd
    formatarData(valor) {
      if (valor instanceof Date && !Number.isNaN(valor.getTime())) {
        // XLSX com cellDates devolve a data em UTC deslocada; usa os campos locais
        const a = valor.getFullYear();
        const m = String(valor.getMonth() + 1).padStart(2, "0");
        const d = String(valor.getDate()).padStart(2, "0");
        return `${a}-${m}-${d}`;
      }
      if (typeof valor === "number" && valor > 20000 && valor < 80000) {
        const dt = new Date(Math.round((valor - 25569) * 86400 * 1000));
        return dt.toISOString().slice(0, 10);
      }
      return String(valor ?? "").trim();
    },

    // Monta o corpo esperado pelo backend (EnvioValidadasRequest)
    montarEnvio() {
      const txt = (v) => (v === null || v === undefined ? "" : String(v).trim());
      return {
        arquivoNome: this.arquivo?.name || "planilha",
        descartadas: 0,
        linhas: this.dadosTratados.map((c, i) => ({
          numeroLinha: i + 2,
          codigoCliente: txt(c.codigo_cliente === "-" ? "" : c.codigo_cliente),
          nomeCliente: txt(c.nome_cliente === "Sem nome" ? "" : c.nome_cliente),
          consultor: txt(c.consultor),
          segmento: txt(c.segmento === "-" ? "" : c.segmento),
          nivelCliente: txt(c.nivel_cliente === "N/A" ? "" : c.nivel_cliente),
          faturamentoAnual: txt(c.faturamento_anual),
          servicosContratados: txt(c.servicos_contratados ?? c.servicos ?? c.servico),
          dataContratacao: this.formatarData(c.data_contratacao ?? c.data),
          cidade: txt(c.cidade ?? c.localidade),
          uf: txt(c.uf),
        })),
      };
    },

    // 2ª validação no backend + envio ao Python (que grava no banco e calcula)
    async continuarParaInsights() {
      this.enviando = true;
      this.erros = [];
      try {
        const { data } = await api.post("/api/upload/validadas", this.montarEnvio());
        this.loteId = data.loteId ?? null;
        const url = this.loteId ? `/api/insights/${this.loteId}` : "/api/insights/ultimo";
        const resp = await api.get(url);
        this.resultado = resp.data;
        return true;
      } catch (e) {
        const msg = e.response?.data?.message || e.response?.data?.error || e.message;
        this.erros = [`Não foi possível processar a planilha: ${msg}`];
        return false;
      } finally {
        this.enviando = false;
      }
    },

    // Abre os insights de um lote do histórico
    async abrirLote(id) {
      this.loteId = id;
      const resp = await api.get(`/api/insights/${id}`);
      this.resultado = resp.data;
    },

    async carregarUltimoInsight() {
      try {
        const resp = await api.get(this.loteId ? `/api/insights/${this.loteId}` : "/api/insights/ultimo");
        this.resultado = resp.data;
      } catch (e) {
        this.resultado = null;
      }
    },
  },
});
