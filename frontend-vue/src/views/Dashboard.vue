<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'
import { useUploadStore } from '../stores/uploadStore.js'

const router = useRouter()
const auth = useAuthStore()
const upload = useUploadStore()

const lotes = ref([])
const insight = ref(null)
const insightLoteId = ref(null)
const carregando = ref(true)
const erro = ref('')
const erroInsights = ref('')
const abrindoInsight = ref(false)

const primeiroNome = computed(() => auth.usuario?.nome?.trim().split(/\s+/)[0] || 'por aqui')
const saudacao = computed(() => {
  const hora = new Date().getHours()
  if (hora < 12) return 'Bom dia'
  if (hora < 18) return 'Boa tarde'
  return 'Boa noite'
})
const dataHoje = new Intl.DateTimeFormat('pt-BR', {
  weekday: 'long',
  day: 'numeric',
  month: 'long',
}).format(new Date())

const processados = computed(() => lotes.value.filter((lote) => lote.status === 'PROCESSADO'))
const errosLote = computed(() => lotes.value.filter((lote) => lote.status === 'ERRO').length)
const ultimoProcessado = computed(() => processados.value[0] || null)
const ultimosLotes = computed(() => lotes.value.slice(0, 5))
const linhasValidas = computed(() =>
  lotes.value.reduce((total, lote) => total + (Number(lote.linhasValidas) || 0), 0)
)
const taxaSucesso = computed(() =>
  lotes.value.length ? Math.round((processados.value.length / lotes.value.length) * 100) : 0
)
const resumoInsight = computed(() => insight.value?.resumo || {})

const cartoes = computed(() => [
  {
    titulo: 'Importações',
    valor: carregando.value ? '—' : lotes.value.length,
    detalhe: 'lotes registrados',
    cor: 'bg-[#edf4e5] text-[#52761e]',
    icone: 'upload',
  },
  {
    titulo: 'Linhas válidas',
    valor: carregando.value ? '—' : new Intl.NumberFormat('pt-BR').format(linhasValidas.value),
    detalhe: 'somadas entre os lotes',
    cor: 'bg-[#eef2fc] text-[#5b6eaa]',
    icone: 'users',
  },
  {
    titulo: 'Taxa de sucesso',
    valor: carregando.value ? '—' : `${taxaSucesso.value}%`,
    detalhe: `${processados.value.length} lotes processados`,
    cor: 'bg-[#f5efe3] text-[#a06e22]',
    icone: 'chart',
  },
  {
    titulo: 'Com atenção',
    valor: carregando.value ? '—' : errosLote.value,
    detalhe: 'lotes com falha',
    cor: 'bg-[#faece9] text-[#b44d3c]',
    icone: 'alert',
  },
])

function dataHora(valor) {
  if (!valor) return 'Data indisponível'
  const data = new Date(valor)
  if (Number.isNaN(data.getTime())) return 'Data indisponível'
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  }).format(data)
}

function moeda(valor) {
  if (valor === null || valor === undefined || !Number.isFinite(Number(valor))) return '—'
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
    maximumFractionDigits: 0,
  }).format(Number(valor))
}

function statusLabel(status) {
  return {
    PROCESSADO: 'Processado',
    PROCESSANDO: 'Processando',
    ERRO: 'Com erro',
  }[status] || status || 'Indefinido'
}

function statusClass(status) {
  return {
    PROCESSADO: 'bg-[#edf4e5] text-[#52761e]',
    PROCESSANDO: 'bg-[#fff6df] text-[#9a6b16]',
    ERRO: 'bg-[#fff0ed] text-[#b44d3c]',
  }[status] || 'bg-[#f0f2ed] text-[#70786b]'
}

async function carregar() {
  carregando.value = true
  erro.value = ''
  erroInsights.value = ''
  insight.value = null
  insightLoteId.value = null

  try {
    const { data } = await api.get('/api/lotes')
    if (!Array.isArray(data)) throw new Error('A resposta do histórico está em um formato inesperado.')
    lotes.value = data
  } catch (requestError) {
    erro.value = requestError.response?.data?.message || requestError.message || 'Não foi possível carregar os dados do dashboard.'
    lotes.value = []
    carregando.value = false
    return
  }

  carregando.value = false
  const lote = lotes.value.find((item) => item.status === 'PROCESSADO')
  if (!lote) return

  try {
    const { data } = await api.get(`/api/insights/${lote.id}`)
    insight.value = data
    insightLoteId.value = lote.id
  } catch (requestError) {
    erroInsights.value = requestError.response?.data?.message || 'Não foi possível carregar os insights do lote mais recente.'
  }
}

async function verInsights(loteId = insightLoteId.value) {
  if (!loteId) return
  abrindoInsight.value = true
  erroInsights.value = ''
  try {
    await upload.abrirLote(loteId)
    router.push('/insights')
  } catch (requestError) {
    erroInsights.value = requestError.response?.data?.message || 'Não foi possível abrir os insights deste lote.'
  } finally {
    abrindoInsight.value = false
  }
}

onMounted(carregar)
</script>

<template>
  <main class="min-h-[calc(100vh-5rem)] px-5 py-8 sm:px-8 sm:py-10 lg:px-12">
    <div class="mx-auto max-w-325">
      <header class="mb-8 flex flex-col justify-between gap-5 sm:flex-row sm:items-end">
        <div>
          <div class="mb-3 flex items-center gap-2 text-xs font-bold uppercase tracking-[0.18em] text-[#84917b]">
            <span class="h-2 w-2 rounded-full bg-[#8db84e]"></span>
            {{ dataHoje }}
          </div>
          <h1 class="font-aclonica text-3xl leading-tight text-[#26351f] sm:text-4xl">{{ saudacao }}, {{ primeiroNome }}.</h1>
          <p class="mt-2 max-w-2xl text-sm leading-6 text-[#70786b]">Aqui está o panorama das suas análises e importações.</p>
        </div>
        <div class="flex flex-wrap gap-2">
          <button type="button" class="rounded-xl border border-[#d8e1d1] bg-white px-4 py-2.5 text-sm font-bold text-[#52761e] transition hover:border-[#52761e] hover:bg-[#f0f5eb] disabled:opacity-60" :disabled="carregando" @click="carregar">
            {{ carregando ? 'Atualizando...' : 'Atualizar dados' }}
          </button>
          <RouterLink to="/upload" class="inline-flex items-center gap-2 rounded-xl bg-[#52761e] px-4 py-2.5 text-sm font-bold text-white shadow-[0_8px_18px_rgba(82,118,30,0.18)] transition hover:bg-[#3f5e15]">
            <svg aria-hidden="true" class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 16V4m0 0L8 8m4-4 4 4" /><path d="M4 16v3a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-3" /></svg>
            Nova importação
          </RouterLink>
        </div>
      </header>

      <div v-if="erro" role="alert" class="mb-6 flex flex-col gap-3 rounded-2xl border border-[#f0c8c0] bg-[#fff0ed] p-4 text-sm text-[#a13f36] sm:flex-row sm:items-center sm:justify-between">
        <span>{{ erro }}</span>
        <button type="button" class="shrink-0 font-bold underline underline-offset-2" @click="carregar">Tentar novamente</button>
      </div>

      <section class="relative mb-6 overflow-hidden rounded-[1.75rem] bg-[#26351f] p-6 text-white shadow-[0_18px_40px_rgba(38,53,31,0.13)] sm:p-8 lg:p-9">
        <div aria-hidden="true" class="absolute -right-14 -top-32 h-80 w-80 rounded-full border border-white/10"></div>
        <div aria-hidden="true" class="absolute -right-2 -top-20 h-56 w-56 rounded-full border border-white/10"></div>
        <div class="relative grid gap-8 lg:grid-cols-[1fr_auto] lg:items-center">
          <div class="max-w-2xl">
            <span class="inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/10 px-3 py-1.5 text-[11px] font-bold uppercase tracking-[0.14em] text-[#c8dda6]">
              <span class="h-1.5 w-1.5 rounded-full bg-[#b7d887]"></span>
              Seu espaço de análise
            </span>
            <h2 class="mt-5 font-aclonica text-2xl leading-snug sm:text-3xl">Dados organizados.<br class="hidden sm:block" /> Decisões com mais clareza.</h2>
            <p class="mt-3 max-w-xl text-sm leading-6 text-[#d5ddcf]">Importe uma planilha para transformar seus dados de clientes em insights práticos para o negócio.</p>
            <RouterLink to="/upload" class="group mt-6 inline-flex items-center gap-2 rounded-xl bg-[#b7d887] px-4 py-3 text-sm font-bold text-[#26351f] transition hover:bg-[#c7e29f]">
              Começar uma análise
              <svg aria-hidden="true" class="h-4 w-4 transition-transform group-hover:translate-x-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14m-6-6 6 6-6 6" /></svg>
            </RouterLink>
          </div>

          <div class="grid grid-cols-2 gap-3 lg:w-64">
            <div class="rounded-2xl border border-white/10 bg-white/[0.07] p-4">
              <p class="text-[11px] font-bold uppercase tracking-[0.12em] text-[#aebbaa]">Último envio</p>
              <p class="mt-2 text-sm font-bold text-white">{{ ultimoProcessado ? dataHora(ultimoProcessado.enviadoEm) : 'Ainda não há envios' }}</p>
            </div>
            <div class="rounded-2xl border border-white/10 bg-white/[0.07] p-4">
              <p class="text-[11px] font-bold uppercase tracking-[0.12em] text-[#aebbaa]">Taxa de sucesso</p>
              <p class="mt-2 font-aclonica text-2xl text-[#c8dda6]">{{ carregando ? '—' : `${taxaSucesso}%` }}</p>
            </div>
            <div class="col-span-2 rounded-2xl border border-white/10 bg-white/[0.07] p-4">
              <div class="flex items-center justify-between gap-3 text-xs">
                <span class="font-semibold text-[#d5ddcf]">Processamento dos lotes</span>
                <span class="font-bold text-white">{{ carregando ? '—' : `${processados.length} de ${lotes.length}` }}</span>
              </div>
              <div class="mt-3 h-1.5 overflow-hidden rounded-full bg-white/15">
                <div class="h-full rounded-full bg-[#b7d887] transition-[width] duration-500" :style="{ width: `${taxaSucesso}%` }"></div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="grid gap-3 sm:grid-cols-2 xl:grid-cols-4" aria-label="Indicadores gerais">
        <article v-for="(cartao, index) in cartoes" :key="cartao.titulo" class="group rounded-2xl border border-[#e2e8dd] bg-white p-5 shadow-[0_7px_22px_rgba(38,53,31,0.035)] transition duration-200 hover:-translate-y-0.5 hover:shadow-[0_12px_28px_rgba(38,53,31,0.08)]">
          <div class="flex items-start justify-between gap-3">
            <div>
              <p class="text-xs font-bold text-[#84917b]">{{ cartao.titulo }}</p>
              <p class="mt-3 font-aclonica text-3xl text-[#26351f]">{{ cartao.valor }}</p>
            </div>
            <span class="grid h-10 w-10 place-items-center rounded-xl" :class="cartao.cor">
              <svg v-if="cartao.icone === 'upload'" aria-hidden="true" class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 16V4m0 0L8 8m4-4 4 4" /><path d="M4 16v3a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-3" /></svg>
              <svg v-else-if="cartao.icone === 'users'" aria-hidden="true" class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="10" cy="7" r="4" /><path d="M20 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" /></svg>
              <svg v-else-if="cartao.icone === 'chart'" aria-hidden="true" class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19V5m0 14h16" /><path d="m7 15 3-3 3 2 5-6" /><path d="M17 8h3v3" /></svg>
              <svg v-else aria-hidden="true" class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="m10.3 3.9-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.7-3l-8-14a2 2 0 0 0-3.4-.1Z" /><path d="M12 9v4m0 4h.01" /></svg>
            </span>
          </div>
          <p class="mt-3 text-xs text-[#9aa493]">{{ cartao.detalhe }}</p>
        </article>
      </section>

      <div class="mt-6 grid gap-6 lg:grid-cols-[1.45fr_0.85fr]">
        <section class="overflow-hidden rounded-2xl border border-[#e2e8dd] bg-white shadow-[0_7px_22px_rgba(38,53,31,0.035)]">
          <div class="flex items-center justify-between gap-4 border-b border-[#edf0e9] px-5 py-5 sm:px-6">
            <div>
              <p class="text-xs font-bold uppercase tracking-[0.14em] text-[#84917b]">Atividade</p>
              <h2 class="mt-1 font-aclonica text-xl text-[#26351f]">Importações recentes</h2>
            </div>
            <RouterLink to="/historico" class="shrink-0 text-xs font-bold text-[#52761e] transition hover:text-[#3f5e15]">Ver histórico <span aria-hidden="true">→</span></RouterLink>
          </div>

          <div v-if="carregando" class="space-y-4 p-5 sm:p-6">
            <div v-for="item in 4" :key="item" class="h-14 animate-pulse rounded-xl bg-[#f3f5f0]"></div>
          </div>
          <div v-else-if="!ultimosLotes.length" class="px-6 py-10 text-center">
            <div class="mx-auto grid h-12 w-12 place-items-center rounded-2xl bg-[#edf4e5] text-[#52761e]">
              <svg aria-hidden="true" class="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 16V4m0 0L8 8m4-4 4 4" /><path d="M4 16v3a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-3" /></svg>
            </div>
            <h3 class="mt-4 font-aclonica text-lg text-[#26351f]">Seu painel começa aqui</h3>
            <p class="mx-auto mt-2 max-w-sm text-sm leading-6 text-[#788273]">Envie sua primeira planilha e acompanhe o processamento e os resultados nesta tela.</p>
            <RouterLink to="/upload" class="mt-5 inline-flex rounded-xl bg-[#52761e] px-4 py-2.5 text-sm font-bold text-white transition hover:bg-[#3f5e15]">Fazer primeira importação</RouterLink>
          </div>
          <ul v-else class="divide-y divide-[#edf0e9]">
            <li v-for="lote in ultimosLotes" :key="lote.id" class="flex flex-col gap-3 px-5 py-4 transition hover:bg-[#fafbf8] sm:flex-row sm:items-center sm:justify-between sm:px-6">
              <div class="flex min-w-0 items-center gap-3">
                <span class="grid h-10 w-10 shrink-0 place-items-center rounded-xl bg-[#f2f5ee] text-xs font-bold text-[#6d7768]">#{{ lote.id }}</span>
                <div class="min-w-0">
                  <p class="truncate text-sm font-bold text-[#26351f]" :title="lote.arquivoNome">{{ lote.arquivoNome || 'Planilha sem nome' }}</p>
                  <p class="mt-1 text-xs text-[#9aa493]">{{ dataHora(lote.enviadoEm) }} <span v-if="lote.linhasValidas !== null && lote.linhasValidas !== undefined">· {{ lote.linhasValidas }} linhas válidas</span></p>
                </div>
              </div>
              <span class="ml-[3.25rem] w-fit rounded-full px-2.5 py-1 text-[11px] font-bold sm:ml-0" :class="statusClass(lote.status)">{{ statusLabel(lote.status) }}</span>
            </li>
          </ul>
        </section>

        <div class="grid content-start gap-6">
          <section class="rounded-2xl border border-[#e2e8dd] bg-white p-5 shadow-[0_7px_22px_rgba(38,53,31,0.035)] sm:p-6">
            <div class="flex items-center justify-between gap-3">
              <div>
                <p class="text-xs font-bold uppercase tracking-[0.14em] text-[#84917b]">Última análise</p>
                <h2 class="mt-1 font-aclonica text-xl text-[#26351f]">Resumo do lote</h2>
              </div>
              <span class="grid h-10 w-10 place-items-center rounded-xl bg-[#edf4e5] text-[#52761e]">
                <svg aria-hidden="true" class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18" /><path d="m19 9-5 5-4-4-5 5" /></svg>
              </span>
            </div>

            <div v-if="carregando" class="mt-5 space-y-3">
              <div class="h-12 animate-pulse rounded-xl bg-[#f3f5f0]"></div>
              <div class="h-12 animate-pulse rounded-xl bg-[#f3f5f0]"></div>
            </div>
            <div v-else-if="!ultimoProcessado" class="mt-5 rounded-xl bg-[#f7f8f3] p-4">
              <p class="text-sm font-bold text-[#3d4739]">Nenhuma análise processada ainda</p>
              <p class="mt-1 text-xs leading-5 text-[#788273]">Quando uma planilha for analisada, os principais resultados aparecem aqui.</p>
            </div>
            <template v-else>
              <p class="mt-5 truncate text-xs text-[#84917b]" :title="ultimoProcessado.arquivoNome">{{ ultimoProcessado.arquivoNome }}</p>
              <div class="mt-3 grid grid-cols-2 gap-3">
                <div class="rounded-xl bg-[#f7f8f3] p-3.5">
                  <p class="text-[11px] font-bold text-[#84917b]">Faturamento</p>
                  <p class="mt-1.5 text-base font-bold text-[#26351f]">{{ moeda(resumoInsight.faturamentoTotal ?? ultimoProcessado.faturamentoTotal) }}</p>
                </div>
                <div class="rounded-xl bg-[#f7f8f3] p-3.5">
                  <p class="text-[11px] font-bold text-[#84917b]">Clientes</p>
                  <p class="mt-1.5 text-base font-bold text-[#26351f]">{{ new Intl.NumberFormat('pt-BR').format(resumoInsight.totalClientes ?? ultimoProcessado.totalClientes ?? ultimoProcessado.linhasValidas ?? 0) }}</p>
                </div>
              </div>
              <div v-if="resumoInsight.segmentoMaisFrequente || resumoInsight.servicoMaisContratado" class="mt-4 space-y-2 border-t border-[#edf0e9] pt-4">
                <p v-if="resumoInsight.segmentoMaisFrequente" class="flex justify-between gap-3 text-xs"><span class="text-[#84917b]">Segmento frequente</span><span class="truncate font-bold text-[#3d4739]">{{ resumoInsight.segmentoMaisFrequente }}</span></p>
                <p v-if="resumoInsight.servicoMaisContratado" class="flex justify-between gap-3 text-xs"><span class="text-[#84917b]">Serviço em destaque</span><span class="truncate font-bold text-[#3d4739]">{{ resumoInsight.servicoMaisContratado }}</span></p>
              </div>
              <p v-if="erroInsights" role="status" class="mt-4 text-xs leading-5 text-[#a06e22]">{{ erroInsights }}</p>
              <button type="button" class="mt-5 flex w-full items-center justify-center gap-2 rounded-xl border border-[#d8e1d1] px-4 py-2.5 text-sm font-bold text-[#52761e] transition hover:bg-[#f0f5eb] disabled:opacity-60" :disabled="abrindoInsight || !insightLoteId" @click="verInsights()">
                {{ abrindoInsight ? 'Abrindo...' : 'Explorar insights' }}
                <svg aria-hidden="true" class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14m-6-6 6 6-6 6" /></svg>
              </button>
            </template>
          </section>

          <section class="rounded-2xl border border-[#e2e8dd] bg-[#f0f5eb] p-5 sm:p-6">
            <div class="flex items-start gap-3">
              <span class="grid h-9 w-9 shrink-0 place-items-center rounded-xl bg-white text-[#52761e]">
                <svg aria-hidden="true" class="h-4.5 w-4.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3 14.5 9.5 21 12l-6.5 2.5L12 21l-2.5-6.5L3 12l6.5-2.5L12 3Z" /><path d="m19 3 .7 1.8L21.5 5.5l-1.8.7L19 8l-.7-1.8-1.8-.7 1.8-.7L19 3Z" /></svg>
              </span>
              <div>
                <h2 class="font-aclonica text-base text-[#26351f]">Uma dica para começar</h2>
                <p class="mt-1.5 text-xs leading-5 text-[#6d7768]">Mantenha os dados de faturamento, segmento e consultor preenchidos para obter análises mais completas.</p>
                <RouterLink to="/upload" class="mt-3 inline-flex items-center gap-1 text-xs font-bold text-[#52761e] hover:underline">Preparar uma planilha <span aria-hidden="true">→</span></RouterLink>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  </main>
</template>
