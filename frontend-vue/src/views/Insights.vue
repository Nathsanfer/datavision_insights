<script setup>
import { computed, onMounted } from "vue";
import { useUploadStore } from "../stores/uploadStore.js";

const upload = useUploadStore();

onMounted(() => {
  if (!upload.resultado) upload.carregarUltimoInsight();
});

const r = computed(() => upload.resultado);
const resumo = computed(() => r.value?.resumo || {});

const moeda = (v) =>
  v === null || v === undefined
    ? "-"
    : new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL", maximumFractionDigits: 0 }).format(v);
const inteiro = (v) => (v === null || v === undefined ? "-" : new Intl.NumberFormat("pt-BR").format(v));

const frases = computed(() =>
  (r.value?.insights || []).map((i) => (typeof i === "string" ? i : i.texto || i.mensagem || i.descricao || JSON.stringify(i)))
);

const cartoes = computed(() => [
  { titulo: "Clientes", valor: inteiro(resumo.value.totalClientes) },
  { titulo: "Faturamento total", valor: moeda(resumo.value.faturamentoTotal) },
  { titulo: "Média", valor: moeda(resumo.value.media) },
  { titulo: "Mediana", valor: moeda(resumo.value.mediana) },
  { titulo: "Segmento mais frequente", valor: resumo.value.segmentoMaisFrequente || "-" },
  { titulo: "Serviço mais contratado", valor: resumo.value.servicoMaisContratado || "-" },
]);

const maximo = (lista, campo) => Math.max(1, ...(lista || []).map((x) => x[campo] || 0));
const largura = (v, max) => `${Math.max(3, Math.round(((v || 0) / max) * 100))}%`;
</script>

<template>
  <div class="min-h-[calc(100vh-5rem)] bg-[#f7f8f3] px-5 py-8 sm:px-8 lg:px-12 lg:py-10">
    <div class="mx-auto max-w-325">
      <p class="mb-2 text-xs font-bold uppercase tracking-[0.22em] text-[#52761e]">Análise</p>
      <h1 class="font-aclonica text-3xl text-[#26351f] sm:text-4xl">Insights</h1>

      <div v-if="!r" class="mt-8 rounded-2xl border border-[#e0e8da] bg-white p-8 text-center text-[#788273]">
        Nenhum insight disponível ainda.
        <RouterLink to="/upload" class="font-bold text-[#52761e] underline">Envie uma planilha</RouterLink>.
      </div>

      <template v-else>
        <div class="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <div v-for="c in cartoes" :key="c.titulo" class="rounded-2xl border border-[#e0e8da] bg-white p-5">
            <p class="text-xs font-bold uppercase tracking-[0.12em] text-[#8b9784]">{{ c.titulo }}</p>
            <p class="mt-2 text-2xl font-bold text-[#26351f]">{{ c.valor }}</p>
          </div>
        </div>

        <div v-if="frases.length" class="mt-8 rounded-3xl bg-[#26351f] p-7 text-white">
          <h2 class="font-aclonica text-xl">O que os dados dizem</h2>
          <ul class="mt-4 list-disc space-y-2 pl-5 text-sm leading-6 text-[#d5ddcf]">
            <li v-for="(f, i) in frases" :key="i">{{ f }}</li>
          </ul>
        </div>

        <div class="mt-8 grid gap-6 lg:grid-cols-2">
          <section class="rounded-2xl border border-[#e0e8da] bg-white p-6">
            <h2 class="font-aclonica text-xl text-[#26351f]">Clientes por segmento</h2>
            <div v-for="s in r.porSegmento" :key="s.segmento" class="mt-4">
              <div class="flex justify-between text-sm text-[#596353]">
                <span>{{ s.segmento }}</span><span>{{ s.clientes }} · {{ moeda(s.total) }}</span>
              </div>
              <div class="mt-1 h-2 rounded-full bg-[#edf5e6]">
                <div class="h-2 rounded-full bg-[#52761e]" :style="{ width: largura(s.clientes, maximo(r.porSegmento, 'clientes')) }"></div>
              </div>
            </div>
          </section>

          <section class="rounded-2xl border border-[#e0e8da] bg-white p-6">
            <h2 class="font-aclonica text-xl text-[#26351f]">Clientes por nível</h2>
            <div v-for="n in r.porNivel" :key="n.nivel" class="mt-4">
              <div class="flex justify-between text-sm text-[#596353]">
                <span>Nível {{ n.nivel }}</span><span>{{ n.clientes }} · {{ moeda(n.total) }}</span>
              </div>
              <div class="mt-1 h-2 rounded-full bg-[#edf5e6]">
                <div class="h-2 rounded-full bg-[#8db84e]" :style="{ width: largura(n.clientes, maximo(r.porNivel, 'clientes')) }"></div>
              </div>
            </div>
          </section>

          <section class="rounded-2xl border border-[#e0e8da] bg-white p-6">
            <h2 class="font-aclonica text-xl text-[#26351f]">Faturamento por consultor</h2>
            <div v-for="c in r.porConsultor" :key="c.consultor" class="mt-4">
              <div class="flex justify-between text-sm text-[#596353]">
                <span>{{ c.consultor }}</span><span>{{ c.clientes }} · {{ moeda(c.total) }}</span>
              </div>
              <div class="mt-1 h-2 rounded-full bg-[#edf5e6]">
                <div class="h-2 rounded-full bg-[#52761e]" :style="{ width: largura(c.total, maximo(r.porConsultor, 'total')) }"></div>
              </div>
            </div>
          </section>

          <section class="rounded-2xl border border-[#e0e8da] bg-white p-6">
            <h2 class="font-aclonica text-xl text-[#26351f]">Maiores clientes</h2>
            <table class="mt-4 w-full text-left text-sm text-[#596353]">
              <thead><tr class="text-xs uppercase text-[#8b9784]"><th class="py-2">Nome</th><th>Segmento</th><th>Nível</th><th class="text-right">Faturamento</th></tr></thead>
              <tbody>
                <tr v-for="t in r.topClientes" :key="t.codigo" class="border-t border-gray-100">
                  <td class="py-2">{{ t.nome }}</td><td>{{ t.segmento }}</td><td>{{ t.nivel }}</td><td class="text-right">{{ moeda(t.faturamento) }}</td>
                </tr>
              </tbody>
            </table>
          </section>
        </div>

        <section v-if="r.qualidade" class="mt-8 rounded-2xl border border-[#e0e8da] bg-white p-6 text-sm text-[#596353]">
          <h2 class="font-aclonica text-xl text-[#26351f]">Qualidade da planilha</h2>
          <p class="mt-3">Recebidas: {{ r.qualidade.linhasRecebidas }} · Válidas: {{ r.qualidade.linhasValidas }} · Inválidas: {{ r.qualidade.linhasInvalidas }}</p>
        </section>
      </template>
    </div>
  </div>
</template>
