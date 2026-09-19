<script setup>
import { computed, ref } from "vue";
import * as XLSX from "xlsx";
import { useUploadStore } from "../stores/uploadStore.js";

const upload = useUploadStore();
const fileInput = ref(null);

const formatarNumero = (valor) => {
  const numero = Number(valor ?? 0);

  if (Number.isNaN(numero)) return "-";

  return new Intl.NumberFormat("pt-BR", {
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(numero);
};

const progresso = computed(() => {
  if (!upload.arquivo) return 0;
  if (upload.carregamento) return 70;
  if (upload.dadosTratados.length) return 100;
  return 20;
});

function abrirSeletor() {
  fileInput.value?.click();
}

async function aoSelecionaArquivo(event) {
  const file = event.target.files?.[0];
  if (!file) return;

  upload.selecionarArquivo(file);
  await processarArquivo();
}

async function processarArquivo() {
  if (!upload.arquivo) {
    upload.erros = ["Selecione uma planilha antes de processar."];
    return;
  }

  const nome = upload.arquivo.name.toLowerCase();
  const valido = [".xlsx", ".xls", ".csv"].some((ext) => nome.endsWith(ext));

  if (!valido) {
    upload.erros = ["Formato inválido. Envie .xlsx, .xls ou .csv."];
    return;
  }

  upload.carregamento = true;
  upload.erros = [];

  try {
    const buffer = await upload.arquivo.arrayBuffer();
    const workbook = XLSX.read(buffer, { type: "array" });
    const planilha = workbook.Sheets[workbook.SheetNames[0]];
    const linhas = XLSX.utils.sheet_to_json(planilha, { defval: "" });

    upload.dadosOriginais = linhas;
    upload.dadosTratados = linhas.map((linha) => ({
      ...linha,
      codigo_cliente: linha.codigo_cliente || linha.codigo || linha.id || "-",
      nome_cliente: linha.nome_cliente || linha.cliente || linha.nome || "Sem nome",
      segmento: upload.normalizarSegmento(linha.segmento),
      nivel_cliente: String(linha.nivel_cliente || linha.nivel || "N/A")
        .trim()
        .toUpperCase(),
    }));
  } catch (error) {
    upload.erros = ["Não foi possível ler a planilha."];
  } finally {
    upload.carregamento = false;
  }
}
</script>

<template>
  <div class="flex justify-center mt-4 ml-22 mr-22">
    <div
      class="bg-white p-9 rounded-xl shadow-md w-full flex items-center justify-center gap-3 cursor-pointer"
      @click="abrirSeletor"
    >
      <input
        ref="fileInput"
        type="file"
        class="hidden"
        accept=".xlsx, .xls, .csv"
        @change="aoSelecionaArquivo"
      />
      <img src="../assets/icons/file.png" alt="Upload" class="w-8 h-8 mr-4" />
      <p>Clique e Selecione o Arquivo para o Processamento</p>
    </div>
  </div>

  <div
    class="mt-6 ml-22 mr-22 flex justify-center gap-2 items-center rounded-xl h-6"
  >
    <div class="w-full overflow-hidden rounded-full bg-[#e5e7eb] h-3">
      <div
        class="h-full rounded-full bg-[#52761e] transition-all duration-300 ease-out"
        :style="{ width: `${progresso}%` }"
      ></div>
    </div>
    <p class="text-sm font-medium w-10 text-gray-500">{{ progresso }}%</p>
  </div>

  <div class="flex justify-between mt-7 ml-22 mr-22 gap-9">
    <div
      class="flex items-center justify-center w-full p-4 bg-white shadow-md rounded-xl"
    >
      <p>Total de Linhas: {{ upload.totalClientes }}</p>
    </div>
    <div
      class="flex items-center justify-center w-full p-4 bg-white shadow-md rounded-xl"
    >
      <p>Total de Inconsistências: {{ upload.totalErros }}</p>
    </div>
    <div
      class="flex items-center justify-center w-full p-4 bg-white shadow-md rounded-xl"
    >
      <p>Total de Tratamentos: {{ upload.clientesNivelA }}</p>
    </div>
  </div>

  <div v-if="upload.erros.length" class="mt-4 ml-22 mr-22 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
    <ul class="list-disc pl-5">
      <li v-for="(erro, index) in upload.erros" :key="index">{{ erro }}</li>
    </ul>
  </div>

  <div v-if="upload.dadosTratados.length" class="mt-4 ml-22 mr-22 max-w-full overflow-x-auto pb-10">
    <div class="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm mt-5">
      <table class="w-full border-collapse text-left text-sm text-gray-700">
        <thead class="bg-[#52761e] text-[#26351f]">
          <tr>
            <th class="px-4 py-3 font-semibold text-white">Código</th>
            <th class="px-4 py-3 font-semibold text-white">Nome</th>
            <th class="px-4 py-3 font-semibold text-white">Consultor</th>
            <th class="px-4 py-3 font-semibold text-white">Segmento</th>
            <th class="px-4 py-3 font-semibold text-white">Cidade</th>
            <th class="px-4 py-3 font-semibold text-white">Faturamento</th>
            <th class="px-4 py-3 font-semibold text-white">Nível</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(cliente, index) in upload.dadosTratados"
            :key="index"
            class="border-t border-gray-200 hover:bg-[#f9fbf6]"
          >
            <td class="px-4 py-3">{{ cliente.codigo_cliente || '-' }}</td>
            <td class="px-4 py-3">{{ cliente.nome_cliente || '-' }}</td>
            <td class="px-4 py-3">{{ cliente.consultor || '-' }}</td>
            <td class="px-4 py-3">{{ cliente.segmento || '-' }}</td>
            <td class="px-4 py-3">{{ cliente.cidade || cliente.localidade || '-' }}</td>
            <td class="px-4 py-3">R$ {{ formatarNumero(cliente.faturamento_anual) }}</td>
            <td class="px-4 py-3">
              <span
                :class="cliente.nivel_cliente === 'A'
                  ? 'bg-[#edf7df] text-[#52761e]'
                  : cliente.nivel_cliente === 'B'
                    ? 'bg-[#fef3c7] text-[#9a6700]'
                    : 'bg-[#dbeafe] text-[#1d4ed8]'"
                class="inline-flex rounded-full px-2.5 py-1 text-xs font-semibold"
              >
                {{ cliente.nivel_cliente || 'N/A' }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
