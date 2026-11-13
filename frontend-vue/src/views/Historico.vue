<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import api from "../services/api";
import { useAuthStore } from "../stores/auth";
import { useUploadStore } from "../stores/uploadStore.js";

const router = useRouter();
const auth = useAuthStore();
const upload = useUploadStore();

const lotes = ref([]);
const carregando = ref(true);
const erro = ref("");
const busca = ref("");
const filtroStatus = ref("TODOS");
const abrindo = ref(null);
const excluindo = ref(null);
const confirmar = ref(null);

async function carregar() {
  carregando.value = true;
  erro.value = "";
  try {
    const { data } = await api.get("/api/lotes");
    lotes.value = data;
  } catch (e) {
    erro.value = e.response?.data?.message || "Não foi possível carregar o histórico.";
  } finally {
    carregando.value = false;
  }
}

onMounted(carregar);

const filtrados = computed(() => {
  const termo = busca.value.trim().toLowerCase();
  return lotes.value.filter(
    (l) =>
      (filtroStatus.value === "TODOS" || l.status === filtroStatus.value) &&
      (!termo || String(l.arquivoNome || "").toLowerCase().includes(termo))
  );
});

const resumo = computed(() => {
  const ok = lotes.value.filter((l) => l.status === "PROCESSADO");
  return {
    total: lotes.value.length,
    processados: ok.length,
    erros: lotes.value.filter((l) => l.status === "ERRO").length,
    clientes: ok.reduce((s, l) => s + (l.linhasValidas || 0), 0),
    ultimo: lotes.value[0]?.enviadoEm,
  };
});

const dataHora = (iso) =>
  iso
    ? new Date(iso).toLocaleString("pt-BR", { day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit" })
    : "-";

const relativo = (iso) => {
  if (!iso) return "";
  const min = Math.round((Date.now() - new Date(iso).getTime()) / 60000);
  if (min < 1) return "agora mesmo";
  if (min < 60) return `há ${min} min`;
  const h = Math.round(min / 60);
  if (h < 24) return `há ${h} h`;
  const d = Math.round(h / 24);
  return `há ${d} ${d === 1 ? "dia" : "dias"}`;
};

const moeda = (v) =>
  v === null || v === undefined
    ? "-"
    : new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL", maximumFractionDigits: 0 }).format(v);

const estilos = {
  PROCESSADO: "bg-[#edf5e6] text-[#52761e]",
  ERRO: "bg-[#fce9e7] text-[#a13f36]",
  PROCESSANDO: "bg-[#fdf3dc] text-[#b7791f]",
};
const rotulos = { PROCESSADO: "Processado", ERRO: "Erro", PROCESSANDO: "Processando" };

async function verInsights(l) {
  abrindo.value = l.id;
  try {
    await upload.abrirLote(l.id);
    router.push("/insights");
  } catch (e) {
    erro.value = e.response?.data?.message || "Este lote não possui insights.";
  } finally {
    abrindo.value = null;
  }
}

async function excluir(l) {
  excluindo.value = l.id;
  try {
    await api.delete(`/api/lotes/${l.id}`);
    lotes.value = lotes.value.filter((x) => x.id !== l.id);
    if (upload.loteId === l.id) {
      upload.loteId = null;
      upload.resultado = null;
    }
  } catch (e) {
    erro.value = e.response?.data?.message || "Não foi possível excluir o lote.";
  } finally {
    excluindo.value = null;
    confirmar.value = null;
  }
}
</script>

<template>
  <div class="min-h-[calc(100vh-5rem)] bg-[#f7f8f3] px-5 py-8 sm:px-8 lg:px-12 lg:py-10">
    <div class="mx-auto max-w-325">
      <div class="mb-8 flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <p class="mb-2 text-xs font-bold uppercase tracking-[0.22em] text-[#52761e]">Registro de importações</p>
          <h1 class="font-aclonica text-3xl leading-tight text-[#26351f] sm:text-4xl">Histórico</h1>
          <p class="mt-2 max-w-3xl text-sm leading-6 text-[#788273]">Acompanhe cada planilha enviada, quando foi processada e reabra os insights de qualquer lote.</p>
        </div>
        <div class="flex gap-2">
          <button type="button" class="rounded-xl border border-[#d8e1d1] bg-white px-4 py-2.5 text-sm font-bold text-[#52761e] transition hover:border-[#52761e] hover:bg-[#f0f5eb]" @click="carregar">Atualizar</button>
          <RouterLink to="/upload" class="rounded-xl bg-[#52761e] px-4 py-2.5 text-sm font-bold text-white shadow-[0_8px_18px_rgba(82,118,30,0.18)] transition hover:bg-[#3f5e15]">Novo upload</RouterLink>
        </div>
      </div>

      <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div class="rounded-2xl border border-[#e0e8da] bg-white p-5 shadow-[0_8px_25px_rgba(38,53,31,0.04)]">
          <p class="text-xs font-bold uppercase tracking-[0.12em] text-[#8b9784]">Total de lotes</p>
          <p class="mt-2 text-3xl font-bold text-[#26351f]">{{ resumo.total }}</p>
          <p class="mt-1 text-xs text-[#9aa493]">Planilhas enviadas</p>
        </div>
        <div class="rounded-2xl border border-[#e0e8da] bg-white p-5 shadow-[0_8px_25px_rgba(38,53,31,0.04)]">
          <p class="text-xs font-bold uppercase tracking-[0.12em] text-[#8b9784]">Processados</p>
          <p class="mt-2 text-3xl font-bold text-[#52761e]">{{ resumo.processados }}</p>
          <p class="mt-1 text-xs text-[#9aa493]">{{ resumo.clientes }} clientes válidos</p>
        </div>
        <div class="rounded-2xl border border-[#e0e8da] bg-white p-5 shadow-[0_8px_25px_rgba(38,53,31,0.04)]">
          <p class="text-xs font-bold uppercase tracking-[0.12em] text-[#8b9784]">Com erro</p>
          <p class="mt-2 text-3xl font-bold text-[#b5473d]">{{ resumo.erros }}</p>
          <p class="mt-1 text-xs text-[#9aa493]">Falhas no processamento</p>
        </div>
        <div class="rounded-2xl bg-[#26351f] p-5 text-white shadow-[0_16px_45px_rgba(38,53,31,0.12)]">
          <p class="text-xs font-bold uppercase tracking-[0.12em] text-[#b8c3b2]">Último envio</p>
          <p class="mt-2 text-xl font-bold text-[#b7d887]">{{ resumo.ultimo ? relativo(resumo.ultimo) : "-" }}</p>
          <p class="mt-1 text-xs text-[#b8c3b2]">{{ dataHora(resumo.ultimo) }}</p>
        </div>
      </div>

      <div class="mt-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <input v-model="busca" type="search" placeholder="Buscar por nome do arquivo..." class="w-full rounded-xl border border-[#dbe3d4] bg-white px-4 py-2.5 text-sm text-[#26351f] outline-none transition placeholder:text-[#a5ae9f] focus:border-[#52761e] sm:max-w-sm" />
        <div class="flex gap-1 rounded-xl border border-[#e2e8dd] bg-white p-1">
          <button v-for="s in ['TODOS', 'PROCESSADO', 'ERRO']" :key="s" type="button" class="rounded-lg px-3 py-1.5 text-xs font-bold transition" :class="filtroStatus === s ? 'bg-[#52761e] text-white' : 'text-[#6d7768] hover:bg-[#f0f5eb]'" @click="filtroStatus = s">
            {{ s === "TODOS" ? "Todos" : rotulos[s] }}
          </button>
        </div>
      </div>

      <div v-if="erro" class="mt-4 rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{{ erro }}</div>

      <div class="mt-4 overflow-hidden rounded-2xl border border-[#e0e8da] bg-white shadow-[0_10px_30px_rgba(38,53,31,0.05)]">
        <div v-if="carregando" class="p-10 text-center text-sm text-[#788273]">Carregando histórico...</div>

        <div v-else-if="!filtrados.length" class="p-8 text-center sm:p-12">
          <p class="font-aclonica text-xl text-[#26351f]">{{ lotes.length ? "Nenhum lote encontrado" : "Nenhum lote ainda" }}</p>
          <p class="mt-2 mb-5 text-sm text-[#788273]">{{ lotes.length ? "Ajuste a busca ou o filtro." : "Envie sua primeira planilha para começar." }}</p>
          <RouterLink v-if="!lotes.length" to="/upload" class="mt-5 inline-block rounded-xl bg-[#52761e] px-5 py-3 text-sm font-bold text-white">Ir para o upload</RouterLink>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full min-w-[960px] border-collapse text-left text-sm text-[#596353]">
            <thead class="bg-[#26351f] text-white">
              <tr>
                <th class="px-5 py-3 font-semibold">Lote</th>
                <th class="px-5 py-3 font-semibold">Arquivo</th>
                <th class="px-5 py-3 font-semibold">Enviado em</th>
                <th class="px-5 py-3 font-semibold">Status</th>
                <th class="px-5 py-3 text-right font-semibold">Linhas</th>
                <th class="px-5 py-3 text-right font-semibold">Faturamento</th>
                <th class="px-5 py-3 text-right font-semibold">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="l in filtrados" :key="l.id" class="border-t border-[#eef2ea] transition-colors hover:bg-[#f9fbf6]">
                <td class="px-5 py-4 font-bold text-[#26351f]">#{{ l.id }}</td>
                <td class="max-w-[260px] px-5 py-4">
                  <p class="truncate font-semibold text-[#26351f]" :title="l.arquivoNome">{{ l.arquivoNome }}</p>
                  <p v-if="l.status === 'ERRO' && l.mensagem" class="mt-1 truncate text-xs text-[#a13f36]" :title="l.mensagem">{{ l.mensagem }}</p>
                </td>
                <td class="whitespace-nowrap px-5 py-4">
                  <p>{{ dataHora(l.enviadoEm) }}</p>
                  <p class="mt-0.5 text-xs text-[#9aa493]">{{ relativo(l.enviadoEm) }}</p>
                </td>
                <td class="px-5 py-4">
                  <span class="inline-flex rounded-full px-2.5 py-1 text-xs font-bold" :class="estilos[l.status] || 'bg-gray-100 text-gray-600'">{{ rotulos[l.status] || l.status }}</span>
                </td>
                <td class="px-5 py-4 text-right">
                  <p class="font-semibold text-[#26351f]">{{ l.linhasValidas }} <span class="font-normal text-[#9aa493]">/ {{ l.totalLinhas }}</span></p>
                  <p class="mt-0.5 text-xs text-[#9aa493]">
                    <span v-if="l.linhasInvalidas" class="text-[#b5473d]">{{ l.linhasInvalidas }} inválidas</span>
                    <span v-if="l.linhasInvalidas && l.linhasComAviso"> · </span>
                    <span v-if="l.linhasComAviso" class="text-[#b7791f]">{{ l.linhasComAviso }} avisos</span>
                    <span v-if="!l.linhasInvalidas && !l.linhasComAviso">sem pendências</span>
                  </p>
                </td>
                <td class="whitespace-nowrap px-5 py-4 text-right font-semibold text-[#26351f]">{{ moeda(l.faturamentoTotal) }}</td>
                <td class="whitespace-nowrap px-5 py-4 text-right">
                  <template v-if="confirmar === l.id">
                    <span class="mr-2 text-xs font-semibold text-[#a13f36]">Excluir?</span>
                    <button type="button" class="mr-1 rounded-lg bg-[#b5473d] px-3 py-1.5 text-xs font-bold text-white disabled:opacity-60" :disabled="excluindo === l.id" @click="excluir(l)">Sim</button>
                    <button type="button" class="rounded-lg border border-[#d8e1d1] px-3 py-1.5 text-xs font-bold text-[#6d7768]" @click="confirmar = null">Não</button>
                  </template>
                  <template v-else>
                    <button v-if="l.status === 'PROCESSADO'" type="button" class="rounded-lg bg-[#52761e] px-3 py-1.5 text-xs font-bold text-white transition hover:bg-[#3f5e15] disabled:opacity-60" :disabled="abrindo === l.id" @click="verInsights(l)">
                      {{ abrindo === l.id ? "Abrindo..." : "Ver insights" }}
                    </button>
                    <button v-if="auth.isAdmin" type="button" class="ml-2 rounded-lg border border-[#f0c8c3] px-3 py-1.5 text-xs font-bold text-[#a13f36] transition hover:bg-[#fff1ef]" @click="confirmar = l.id">Excluir</button>
                  </template>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <p v-if="!carregando && filtrados.length" class="mt-3 text-xs text-[#9aa493]">Exibindo {{ filtrados.length }} de {{ lotes.length }} lotes.</p>
    </div>
  </div>
</template>
