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
      nome_cliente:
        linha.nome_cliente ||
        linha.nome_empresa ||
        linha.cliente ||
        linha.nome ||
        "Sem nome",
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
  <div class="min-h-[calc(100vh-5rem)] bg-[#f7f8f3] px-5 py-8 sm:px-8 lg:px-12 lg:py-10">
    <div class="mx-auto max-w-325">
      <div class="mb-8 flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <p class="mb-2 text-xs font-bold uppercase tracking-[0.22em] text-[#52761e]">Central de dados</p>
          <h1 class="font-aclonica text-3xl leading-tight text-[#26351f] sm:text-4xl">Importe seus dados</h1>
          <p class="mt-2 max-w-3xl text-sm leading-6 text-[#788273]">Envie uma planilha para organizar clientes, identificar inconsistências e preparar seus insights.</p>
        </div>
        <span class="inline-flex w-fit items-center gap-2 rounded-full border border-[#dbe7cf] bg-[#edf5e6] px-3 py-2 text-xs font-bold text-[#52761e]"><span class="h-2 w-2 rounded-full bg-[#8db84e]"></span>Processamento local</span>
      </div>

      <div class="grid gap-6 lg:grid-cols-[minmax(0,1.35fr)_minmax(280px,0.65fr)]">
        <button type="button" class="group relative overflow-hidden rounded-3xl border border-dashed border-[#b8cda3] bg-white p-7 text-left shadow-[0_16px_45px_rgba(38,53,31,0.07)] transition hover:-translate-y-0.5 hover:border-[#52761e] hover:shadow-[0_20px_55px_rgba(38,53,31,0.11)] sm:p-10" :disabled="upload.carregamento" @click="abrirSeletor">
          <input ref="fileInput" type="file" class="hidden" accept=".xlsx, .xls, .csv" @change="aoSelecionaArquivo" />
          <div class="absolute -right-16 -top-20 h-48 w-48 rounded-full bg-[#edf5e6] transition-transform duration-500 group-hover:scale-110"></div>
          <div class="relative flex flex-col items-center text-center sm:flex-row sm:items-start sm:text-left">
            <span class="mb-5 grid h-16 w-16 shrink-0 place-items-center rounded-2xl bg-[#52761e] text-white shadow-[0_10px_24px_rgba(82,118,30,0.2)] sm:mr-6 sm:mb-0">
              <svg aria-hidden="true" class="h-8 w-8" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><path d="M14 2v6h6M8 13h8M8 17h5" /></svg>
            </span>
            <span class="relative min-w-0 flex-1">
              <span class="block text-lg font-bold text-[#26351f]">{{ upload.arquivo ? 'Arquivo selecionado' : 'Escolha uma planilha' }}</span>
              <span class="mt-2 block truncate text-sm text-[#788273]">{{ upload.arquivo ? upload.arquivo.name : 'Clique aqui para procurar no seu computador' }}</span>
              <span class="mt-5 inline-flex items-center gap-2 rounded-lg bg-[#f0f5eb] px-3 py-2 text-xs font-bold text-[#52761e]">{{ upload.carregamento ? 'Processando arquivo...' : 'Selecionar arquivo' }} <span aria-hidden="true">↗</span></span>
              <span class="mt-4 block text-xs text-[#9aa493]">Formatos aceitos: XLSX, XLS e CSV</span>
            </span>
          </div>
        </button>

        <div class="rounded-3xl bg-[#26351f] p-7 text-white shadow-[0_16px_45px_rgba(38,53,31,0.12)] sm:p-8">
          <div class="flex items-center justify-between"><span class="text-sm font-semibold text-[#d5ddcf]">Status da importação</span><span class="font-aclonica text-2xl text-[#b7d887]">{{ progresso }}%</span></div>
          <div class="mt-5 h-2 overflow-hidden rounded-full bg-white/15"><div class="h-full rounded-full bg-[#b7d887] transition-all duration-300" :style="{ width: `${progresso}%` }"></div></div>
          <p class="mt-5 text-sm leading-6 text-[#b8c3b2]">{{ upload.carregamento ? 'Estamos lendo e organizando seus registros.' : upload.dadosTratados.length ? 'Dados prontos para análise.' : 'Aguardando o envio da sua planilha.' }}</p>
          <div class="mt-7 flex items-center gap-3 border-t border-white/10 pt-5 text-xs text-[#b8c3b2]"><svg class="h-5 w-5 text-[#b7d887]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M12 3 5 6v5c0 4.5 2.9 8.5 7 10 4.1-1.5 7-5.5 7-10V6l-7-3Z" /><path d="m9 12 2 2 4-4" /></svg> Seus dados permanecem no ambiente local</div>
        </div>
      </div>

      <div class="mt-6 grid gap-4 sm:grid-cols-3">
        <div class="rounded-2xl border border-[#e0e8da] bg-white p-5 shadow-[0_8px_25px_rgba(38,53,31,0.04)]"><p class="text-xs font-bold uppercase tracking-[0.12em] text-[#8b9784]">Total de linhas</p><p class="mt-2 text-3xl font-bold text-[#26351f]">{{ upload.totalClientes }}</p><p class="mt-1 text-xs text-[#9aa493]">Registros encontrados</p></div>
        <div class="rounded-2xl border border-[#e0e8da] bg-white p-5 shadow-[0_8px_25px_rgba(38,53,31,0.04)]"><p class="text-xs font-bold uppercase tracking-[0.12em] text-[#8b9784]">Inconsistências</p><p class="mt-2 text-3xl font-bold text-[#b7791f]">{{ upload.inconsistencias.length }}</p><p class="mt-1 text-xs text-[#9aa493]">Pontos para revisar</p></div>
        <div class="rounded-2xl border border-[#e0e8da] bg-white p-5 shadow-[0_8px_25px_rgba(38,53,31,0.04)]"><p class="text-xs font-bold uppercase tracking-[0.12em] text-[#8b9784]">Clientes nível A</p><p class="mt-2 text-3xl font-bold text-[#52761e]">{{ upload.clientesNivelA }}</p><p class="mt-1 text-xs text-[#9aa493]">Alta prioridade</p></div>
      </div>

  <div v-if="upload.erros.length" class="mt-6 rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
    <ul class="list-disc pl-5">
      <li v-for="(erro, index) in upload.erros" :key="index">{{ erro }}</li>
    </ul>
  </div>

  <div v-if="upload.dadosTratados.length" class="mt-6 overflow-x-auto pb-10">
    <div class="mb-3 flex items-center justify-between"><div><h2 class="font-aclonica text-2xl text-[#26351f]">Prévia dos dados</h2><p class="mt-1 text-sm text-[#8b9784]">Confira os registros tratados antes de continuar.</p></div><span class="rounded-full bg-[#edf5e6] px-3 py-1.5 text-xs font-bold text-[#52761e]">{{ upload.dadosTratados.length }} registros</span></div>
    <div class="overflow-hidden rounded-2xl border border-[#e0e8da] bg-white shadow-[0_10px_30px_rgba(38,53,31,0.05)]">
      <table class="w-full min-w-[900px] border-collapse text-left text-sm text-[#596353]">
        <thead class="bg-[#26351f] text-white">
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
            v-for="(cliente, index) in upload.dadosTratados.slice(0, 10)"
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
                  ? 'bg-[#51761ece] text-[#fff]'
                  : cliente.nivel_cliente === 'B'
                    ? 'bg-[#4572069b] text-[#fff]'
                    : 'bg-[#51761e67] text-[#fff]'"
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

  <div
    v-if="upload.inconsistencias.length"
    class="mt-1 max-w-full overflow-x-auto pb-10"
  >
    <div
      class="overflow-hidden rounded-3xl border border-[#eadfd6] bg-white shadow-[0_12px_35px_rgba(132,73,43,0.07)]"
    >
      <div
        class="relative flex flex-wrap items-center justify-between gap-5 overflow-hidden border-b border-[#eee5df] bg-[#fffaf7] px-6 py-5 sm:px-7"
      >
        <div class="absolute -right-12 -top-16 h-36 w-36 rounded-full bg-[#fce9e7]"></div>
        <div class="flex items-start gap-3">
          <div
            class="relative flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-[#b5473d] text-white shadow-[0_8px_18px_rgba(181,71,61,0.18)]"
            aria-hidden="true"
          >
            <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.3 3.4 2.5 17a2 2 0 0 0 1.7 3h15.6a2 2 0 0 0 1.7-3L13.7 3.4a2 2 0 0 0-3.4 0Z" /><path d="M12 9v4M12 17h.01" /></svg>
          </div>
          <div class="relative">
            <p class="text-[10px] font-bold uppercase tracking-[0.2em] text-[#b5473d]">Qualidade dos dados</p>
            <h2 class="mt-1 font-aclonica text-2xl text-[#3d2a24]">Revisão necessária</h2>
            <p class="mt-1 max-w-xl text-sm leading-6 text-[#806e66]">Encontramos registros que merecem atenção antes de seguir para os insights.</p>
          </div>
        </div>
        <span
          class="relative inline-flex items-center gap-2 rounded-full border border-[#f0c8c3] bg-white px-4 py-2 text-sm font-bold text-[#a13f36] shadow-sm"
        >
          <span class="h-2 w-2 rounded-full bg-[#b5473d]"></span>
          {{ upload.inconsistencias.length }}
          {{ upload.inconsistencias.length === 1 ? "linha" : "linhas" }}
        </span>
      </div>
      <div class="flex items-center gap-2 border-b border-[#f0ebe7] px-6 py-3 text-xs text-[#8b786f] sm:px-7">
        <svg class="h-4 w-4 shrink-0 text-[#b7791f]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M12 9v4M12 17h.01" /><circle cx="12" cy="12" r="9" /></svg>
        Consulte a coluna <span class="font-bold text-[#a13f36]">Problemas encontrados</span> para corrigir a planilha.
      </div>
      <table class="w-full min-w-245 border-collapse text-left text-sm text-[#625850]">
        <thead class="bg-[#f7f3ef] text-[#806e66]">
          <tr>
            <th class="whitespace-nowrap px-5 py-3 text-[11px] font-bold uppercase tracking-[0.12em]">Linha</th>
            <th class="whitespace-nowrap px-5 py-3 text-[11px] font-bold uppercase tracking-[0.12em]">Código</th>
            <th class="whitespace-nowrap px-5 py-3 text-[11px] font-bold uppercase tracking-[0.12em]">Nome</th>
            <th class="whitespace-nowrap px-5 py-3 text-[11px] font-bold uppercase tracking-[0.12em]">Consultor</th>
            <th class="whitespace-nowrap px-5 py-3 text-[11px] font-bold uppercase tracking-[0.12em]">Segmento</th>
            <th class="whitespace-nowrap px-5 py-3 text-[11px] font-bold uppercase tracking-[0.12em]">Faturamento</th>
            <th class="whitespace-nowrap px-5 py-3 text-[11px] font-bold uppercase tracking-[0.12em]">Nível</th>
            <th class="whitespace-nowrap px-5 py-3 text-[11px] font-bold uppercase tracking-[0.12em]">Problemas encontrados</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="cliente in upload.inconsistencias"
            :key="cliente.linha"
            class="border-t border-[#f0ebe7] transition-colors hover:bg-[#fffaf7]"
          >
            <td class="px-5 py-3 font-bold text-[#b5473d]">{{ cliente.linha }}</td>
            <td class="px-5 py-3">{{ cliente.codigo_cliente || "-" }}</td>
            <td class="px-5 py-3 font-semibold text-[#3d2a24]">{{ cliente.nome_cliente || "-" }}</td>
            <td class="px-5 py-3">{{ cliente.consultor || "-" }}</td>
            <td class="px-5 py-3">{{ cliente.segmento || "-" }}</td>
            <td class="px-5 py-3">
              R$ {{ formatarNumero(cliente.faturamento_anual) }}
            </td>
            <td class="px-5 py-3">
              <span
                class="inline-flex min-w-8 justify-center rounded-lg bg-[#fce9e7] px-2.5 py-1 text-xs font-bold text-[#a13f36]"
              >
                {{ cliente.nivel_cliente || "-" }}
              </span>
            </td>
            <td class="px-5 py-3">
              <span class="inline-flex max-w-sm rounded-lg bg-[#fff1ef] px-3 py-1.5 text-xs font-semibold leading-5 text-[#a13f36]">
                {{ cliente.motivos }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
  </div>
  </div>
</template>