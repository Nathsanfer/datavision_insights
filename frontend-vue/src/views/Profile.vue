<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const users = ref([])
const loading = ref(true)
const updatingId = ref(null)
const error = ref('')
const feedback = ref('')

const form = reactive({ nome: '', email: '', senhaAtual: '', novaSenha: '' })
const salvandoDados = ref(false)
const dadosErro = ref('')
const dadosFeedback = ref('')

const isAdmin = computed(() => auth.isAdmin)

function normalizeUser(item) {
    return {
        ...item,
        id: item?.id ?? item?.idUsuario ?? item?.idConsultor,
        name: item?.nome ?? item?.name ?? 'Usuário sem nome',
        email: item?.email ?? 'E-mail não informado',
        phone: item?.telefone ?? item?.phone,
        role: item?.perfil ?? item?.role ?? 'USUÁRIO',
        status: String(item?.status ?? 'ATIVO').toUpperCase(),
    }
}

function initials(item) {
    return normalizeUser(item).name
        .split(' ')
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0])
        .join('')
        .toUpperCase()
}

function statusLabel(status) {
    const labels = { ATIVO: 'Ativo', BLOQUEADO: 'Bloqueado', PENDENTE: 'Pendente', SOLICITADO: 'Pendente' }
    return labels[status] || status
}

function statusClass(status) {
    if (status === 'ATIVO') return 'bg-[#edf4e5] text-[#52761e]'
    if (status === 'PENDENTE' || status === 'SOLICITADO') return 'bg-[#fff6df] text-[#9a6b16]'
    return 'bg-[#fff0ed] text-[#b44d3c]'
}

const currentUser = computed(() => normalizeUser(auth.usuario || {}))

function preencherFormulario() {
    form.nome = auth.usuario?.nome ?? ''
    form.email = auth.usuario?.email ?? ''
    form.senhaAtual = ''
    form.novaSenha = ''
}

async function carregar() {
    // Lista de usuários é exclusiva do administrador; para os demais o endpoint responderia 403.
    if (!isAdmin.value) {
        loading.value = false
        return
    }
    loading.value = true
    error.value = ''
    try {
        const { data } = await api.get('/admin/user/user')
        const payload = Array.isArray(data) ? data : data?.usuarios ?? data?.users ?? (data ? [data] : [])
        users.value = payload.map(normalizeUser)
    } catch (requestError) {
        error.value = requestError.response?.data?.message || 'Não foi possível carregar os usuários agora.'
    } finally {
        loading.value = false
    }
}

// Executa uma ação de administração sobre um usuário e recarrega a lista depois.
async function executarAcao(item, acao, mensagemSucesso) {
    if (!item.id) {
        error.value = 'Não foi possível identificar este usuário.'
        return
    }

    updatingId.value = item.id
    error.value = ''
    feedback.value = ''
    try {
        await acao()
        feedback.value = mensagemSucesso
        await carregar()
    } catch (requestError) {
        error.value = requestError.response?.data?.message || 'Não foi possível concluir a ação.'
    } finally {
        updatingId.value = null
    }
}

function alterarStatus(item, status) {
    const textos = { ATIVO: 'Acesso liberado com sucesso.', BLOQUEADO: 'Usuário bloqueado com sucesso.', PENDENTE: 'Usuário marcado como pendente.' }
    return executarAcao(item, () => api.patch(`/admin/user/${item.id}/status`, { status }), textos[status])
}

function alterarPerfil(item, perfil) {
    return executarAcao(item, () => api.patch(`/admin/user/${item.id}/perfil`, { perfil }), 'Perfil de acesso atualizado.')
}

function excluirUsuario(item) {
    if (!window.confirm(`Excluir o usuário ${item.name}? Esta ação não pode ser desfeita.`)) return
    return executarAcao(item, () => api.delete(`/admin/user/${item.id}`), 'Usuário excluído com sucesso.')
}

async function salvarMeusDados() {
    salvandoDados.value = true
    dadosErro.value = ''
    dadosFeedback.value = ''
    try {
        const { data } = await api.put('/api/auth/me', { ...form })
        // O backend devolve um novo token, pois o e-mail pode ter mudado.
        auth.salvarSessao(data)
        preencherFormulario()
        dadosFeedback.value = 'Seus dados foram atualizados.'
    } catch (requestError) {
        dadosErro.value = requestError.response?.data?.message || 'Não foi possível salvar seus dados.'
    } finally {
        salvandoDados.value = false
    }
}

const pendingUsers = computed(() => users.value.filter((item) => ['PENDENTE', 'SOLICITADO'].includes(item.status)))
const activeUsers = computed(() => users.value.filter((item) => item.status === 'ATIVO'))
const otherUsers = computed(() => users.value.filter((item) => item.id !== currentUser.value.id))

onMounted(() => {
    preencherFormulario()
    carregar()
})
</script>

<template>
    <main class="px-5 py-8 sm:px-8 sm:py-10 lg:px-12">
        <div class="mx-auto max-w-325">
        <div class="mb-9 flex flex-col justify-between gap-5 sm:flex-row sm:items-end">
            <div>
                <p class="mb-2 text-xs font-bold uppercase tracking-[0.22em] text-[#52761e]">Conta e acessos</p>
                <h1 class="font-aclonica text-4xl leading-tight text-[#26351f] sm:text-5xl">Seu perfil</h1>
                <p class="mt-3 max-w-2xl text-sm leading-6 text-[#70786b]">Consulte e atualize seus dados{{ isAdmin ? ', e gerencie quem tem acesso ao ambiente DataVision' : '' }}.</p>
            </div>
            <div class="flex items-center gap-2 rounded-full bg-white px-4 py-2 text-xs font-bold text-[#6d7768] shadow-[0_8px_24px_rgba(32,37,28,0.06)]">
                <span class="h-2 w-2 rounded-full bg-[#8db84e]"></span>
                Ambiente protegido
            </div>
        </div>

        <p v-if="error" class="mb-6 rounded-xl border border-[#f0c8c0] bg-[#fff0ed] px-4 py-3 text-sm text-[#b44d3c]">{{ error }}</p>
        <p v-if="feedback" class="mb-6 rounded-xl border border-[#d4e5c5] bg-[#edf4e5] px-4 py-3 text-sm text-[#52761e]">{{ feedback }}</p>

        <section class="mb-7 grid gap-5" :class="isAdmin ? 'lg:grid-cols-[1.15fr_0.85fr]' : ''">
            <article class="relative overflow-hidden rounded-3xl bg-[#26351f] p-7 text-white shadow-[0_18px_40px_rgba(38,53,31,0.14)] sm:p-9">
                <div class="absolute -right-16 -top-24 h-64 w-64 rounded-full border border-[#b7d084]/20"></div>
                <div class="relative">
                    <p class="text-xs font-bold uppercase tracking-[0.2em] text-[#b7d084]">Minha conta</p>
                    <div class="mt-7 flex items-center gap-4">
                        <div class="grid h-16 w-16 shrink-0 place-items-center rounded-2xl bg-[#b7d084] text-xl font-bold text-[#26351f]">{{ initials(currentUser) }}</div>
                        <div class="min-w-0">
                            <h2 class="truncate font-aclonica text-2xl">{{ currentUser.name }}</h2>
                            <p class="mt-1 truncate text-sm text-[#d5ddcf]">{{ currentUser.email }}</p>
                        </div>
                    </div>
                    <div class="mt-8 grid gap-4 border-t border-white/15 pt-5 sm:grid-cols-2">
                        <div><p class="text-xs text-[#aebbaa]">Perfil de acesso</p><p class="mt-1 text-sm font-bold">{{ currentUser.role }}</p></div>
                        <div><p class="text-xs text-[#aebbaa]">Telefone</p><p class="mt-1 text-sm font-bold">{{ currentUser.phone || 'Não informado' }}</p></div>
                    </div>
                </div>
            </article>
            <div v-if="isAdmin" class="grid gap-5 sm:grid-cols-2 lg:grid-cols-1">
                <article class="rounded-3xl border border-[#e2e8dd] bg-white p-6 shadow-[0_8px_24px_rgba(32,37,28,0.05)]">
                    <p class="text-xs font-bold uppercase tracking-[0.16em] text-[#84917b]">Usuários ativos</p>
                    <p class="mt-3 font-aclonica text-4xl text-[#26351f]">{{ loading ? '—' : activeUsers.length }}</p>
                    <p class="mt-2 text-sm text-[#70786b]">Pessoas com acesso liberado</p>
                </article>
                <article class="rounded-3xl border border-[#e2e8dd] bg-white p-6 shadow-[0_8px_24px_rgba(32,37,28,0.05)]">
                    <p class="text-xs font-bold uppercase tracking-[0.16em] text-[#84917b]">Solicitações</p>
                    <p class="mt-3 font-aclonica text-4xl text-[#9a6b16]">{{ loading ? '—' : pendingUsers.length }}</p>
                    <p class="mt-2 text-sm text-[#70786b]">Aguardando autorização</p>
                </article>
            </div>
        </section>

        <section class="mb-7 rounded-3xl border border-[#e2e8dd] bg-white p-6 shadow-[0_8px_24px_rgba(32,37,28,0.05)] sm:p-8">
            <div class="mb-6">
                <h2 class="font-aclonica text-2xl text-[#26351f]">Meus dados</h2>
                <p class="mt-2 text-sm text-[#70786b]">Atualize seu nome e e-mail. Para trocar a senha, informe a senha atual.</p>
            </div>
            <form class="grid gap-4 sm:grid-cols-2" @submit.prevent="salvarMeusDados">
                <label class="block">
                    <span class="mb-2 block text-sm font-bold text-[#3d4739]">Nome completo</span>
                    <input v-model="form.nome" required maxlength="120" type="text" class="h-11 w-full rounded-xl border border-[#d5dbcf] bg-white px-4 text-sm text-[#20251c] outline-none focus:border-[#52761e] focus:ring-4 focus:ring-[#52761e]/10" />
                </label>
                <label class="block">
                    <span class="mb-2 block text-sm font-bold text-[#3d4739]">E-mail</span>
                    <input v-model="form.email" required maxlength="160" type="email" class="h-11 w-full rounded-xl border border-[#d5dbcf] bg-white px-4 text-sm text-[#20251c] outline-none focus:border-[#52761e] focus:ring-4 focus:ring-[#52761e]/10" />
                </label>
                <label class="block">
                    <span class="mb-2 block text-sm font-bold text-[#3d4739]">Senha atual</span>
                    <input v-model="form.senhaAtual" type="password" autocomplete="current-password" placeholder="Só se for trocar a senha" class="h-11 w-full rounded-xl border border-[#d5dbcf] bg-white px-4 text-sm text-[#20251c] outline-none focus:border-[#52761e] focus:ring-4 focus:ring-[#52761e]/10" />
                </label>
                <label class="block">
                    <span class="mb-2 block text-sm font-bold text-[#3d4739]">Nova senha</span>
                    <input v-model="form.novaSenha" type="password" autocomplete="new-password" placeholder="8+ caracteres, letras e números" class="h-11 w-full rounded-xl border border-[#d5dbcf] bg-white px-4 text-sm text-[#20251c] outline-none focus:border-[#52761e] focus:ring-4 focus:ring-[#52761e]/10" />
                </label>
                <div class="sm:col-span-2 flex flex-col gap-3">
                    <p v-if="dadosErro" class="rounded-xl border border-[#f0c8c0] bg-[#fff0ed] px-4 py-3 text-sm text-[#b44d3c]">{{ dadosErro }}</p>
                    <p v-if="dadosFeedback" class="rounded-xl border border-[#d4e5c5] bg-[#edf4e5] px-4 py-3 text-sm text-[#52761e]">{{ dadosFeedback }}</p>
                    <div><button type="submit" :disabled="salvandoDados" class="rounded-xl bg-[#52761e] px-5 py-2.5 text-sm font-bold text-white transition hover:bg-[#3f5e15] disabled:opacity-50">{{ salvandoDados ? 'Salvando...' : 'Salvar meus dados' }}</button></div>
                </div>
            </form>
        </section>

        <template v-if="isAdmin">
            <section class="rounded-3xl border border-[#e2e8dd] bg-white p-6 shadow-[0_8px_24px_rgba(32,37,28,0.05)] sm:p-8">
                <div class="mb-6 flex items-start justify-between gap-4">
                    <div><h2 class="font-aclonica text-2xl text-[#26351f]">Usuários do sistema</h2><p class="mt-2 text-sm text-[#70786b]">Altere o status e o perfil de cada conta, ou exclua o cadastro.</p></div>
                    <span class="rounded-full bg-[#f0f5eb] px-3 py-1 text-xs font-bold text-[#52761e]">{{ otherUsers.length }} cadastrados</span>
                </div>
                <div v-if="loading" class="space-y-3"><div v-for="item in 3" :key="item" class="h-16 animate-pulse rounded-2xl bg-[#f3f5f0]"></div></div>
                <div v-else-if="!otherUsers.length" class="rounded-2xl bg-[#f7f8f3] px-5 py-8 text-center text-sm text-[#70786b]">Nenhum outro usuário cadastrado.</div>
                <div v-else class="divide-y divide-[#edf0e9]">
                    <div v-for="item in otherUsers" :key="item.id || item.email" class="flex flex-col gap-4 py-4 first:pt-0 last:pb-0 xl:flex-row xl:items-center xl:justify-between">
                        <div class="flex min-w-0 items-center gap-3"><div class="grid h-11 w-11 shrink-0 place-items-center rounded-xl bg-[#edf4e5] text-sm font-bold text-[#52761e]">{{ initials(item) }}</div><div class="min-w-0"><p class="truncate text-sm font-bold text-[#26351f]">{{ item.name }}</p><p class="truncate text-xs text-[#84917b]">{{ item.email }}</p></div></div>
                        <div class="flex flex-wrap items-center gap-2 pl-14 xl:pl-0">
                            <select :value="item.status" :disabled="updatingId === item.id" aria-label="Status do usuário" class="rounded-xl border border-[#d5dbcf] bg-white px-3 py-2 text-xs font-bold text-[#3d4739] outline-none focus:border-[#52761e] disabled:opacity-50" @change="alterarStatus(item, $event.target.value)">
                                <option value="ATIVO">Ativo</option>
                                <option value="PENDENTE">Pendente</option>
                                <option value="BLOQUEADO">Bloqueado</option>
                            </select>
                            <select :value="item.role" :disabled="updatingId === item.id" aria-label="Perfil de acesso" class="rounded-xl border border-[#d5dbcf] bg-white px-3 py-2 text-xs font-bold text-[#3d4739] outline-none focus:border-[#52761e] disabled:opacity-50" @change="alterarPerfil(item, $event.target.value)">
                                <option value="USER">Usuário</option>
                                <option value="ADMIN">Administrador</option>
                            </select>
                            <button type="button" :disabled="updatingId === item.id" class="rounded-xl border border-[#e8c8c0] px-4 py-2 text-xs font-bold text-[#b44d3c] transition hover:bg-[#fff0ed] disabled:opacity-50" @click="excluirUsuario(item)">Excluir</button>
                        </div>
                    </div>
                </div>
            </section>

            <section class="mt-7 rounded-3xl border border-[#eadfbd] bg-[#fffdf6] p-6 shadow-[0_8px_24px_rgba(32,37,28,0.04)] sm:p-8">
                <div class="mb-6 flex items-start justify-between gap-4"><div><p class="mb-2 text-xs font-bold uppercase tracking-[0.16em] text-[#9a6b16]">Gestão de acessos</p><h2 class="font-aclonica text-2xl text-[#26351f]">Solicitações de cadastro</h2><p class="mt-2 text-sm text-[#70786b]">Revise os pedidos antes de liberar o acesso ao sistema.</p></div><span class="rounded-full bg-[#fff6df] px-3 py-1 text-xs font-bold text-[#9a6b16]">{{ pendingUsers.length }} pendentes</span></div>
                <div v-if="!pendingUsers.length" class="rounded-2xl border border-dashed border-[#eadfbd] px-5 py-8 text-center text-sm text-[#70786b]">Tudo certo por aqui. Não há solicitações pendentes.</div>
                <div v-else class="space-y-3">
                    <div v-for="item in pendingUsers" :key="item.id || item.email" class="flex flex-col gap-4 rounded-2xl border border-[#eee5ce] bg-white p-4 sm:flex-row sm:items-center sm:justify-between">
                        <div class="flex items-center gap-3"><div class="grid h-11 w-11 place-items-center rounded-xl bg-[#fff6df] text-sm font-bold text-[#9a6b16]">{{ initials(item) }}</div><div><p class="text-sm font-bold text-[#26351f]">{{ item.name }}</p><p class="text-xs text-[#84917b]">{{ item.email }} <span v-if="item.phone">· {{ item.phone }}</span></p></div></div>
                        <div class="flex gap-2"><button type="button" :disabled="updatingId === item.id" class="rounded-xl bg-[#52761e] px-4 py-2.5 text-xs font-bold text-white transition hover:bg-[#3f5e15] disabled:opacity-50" @click="alterarStatus(item, 'ATIVO')">{{ updatingId === item.id ? 'Salvando...' : 'Autorizar' }}</button><button type="button" :disabled="updatingId === item.id" class="rounded-xl border border-[#e8c8c0] px-4 py-2.5 text-xs font-bold text-[#b44d3c] transition hover:bg-[#fff0ed] disabled:opacity-50" @click="alterarStatus(item, 'BLOQUEADO')">Recusar</button></div>
                    </div>
                </div>
            </section>
        </template>
        </div>
    </main>
</template>
