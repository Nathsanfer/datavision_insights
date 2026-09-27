import { defineStore } from "pinia";
import * as XLSX from "xlsx";

export const useUploadStore = defineStore("upload", {
  // Armazena a quantidade de dados
  state: () => ({
    arquivo: null,
    dadosOriginais: [],
    dadosTratados: [],
    erros: [],
    carregamento: false,
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
          motivos.push("Faturamento deve ser maior que zero");
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
    }
  },
});
