<script setup>
import { ref } from 'vue'
import api from '../services/api'

const name = ref('')
const email = ref('')
const password = ref('')
const mensagem = ref('')
const submitted = ref(false)
const showPassword = ref(false)
const loading = ref(false)

async function cadastrar() {
    try {
        mensagem.value = ''
        loading.value = true
        await api.post('/api/auth/cadastro', {
            nome: name.value,
            email: email.value,
            senha: password.value
        })
        submitted.value = true
        mensagem.value = 'Solicitação enviada. Aguarde a aprovação do administrador.'
    } catch (e) {
        mensagem.value = e.response?.data?.message || 'Erro ao tentar cadastrar. Tente novamente.'
    } finally {
        loading.value = false
    }
}
</script>

<template>
    <main class="register-shell flex h-screen overflow-hidden bg-[#f7f8f3] text-[#20251c] lg:flex-row">
        <section class="brand-panel relative hidden h-screen overflow-hidden bg-[#26351f] text-white lg:flex lg:w-[46%] lg:flex-col lg:justify-between lg:p-14 xl:p-18">
            <div class="brand-grid absolute inset-0 opacity-25"></div>
            <div class="absolute -right-32 top-24 h-80 w-80 rounded-full border border-[#a9c775]/30"></div>
            <div class="absolute -right-20 top-36 h-56 w-56 rounded-full border border-[#a9c775]/20"></div>

            <div class="relative z-10">
                <RouterLink to="/" class="font-aclonica text-2xl tracking-wide">DataVision</RouterLink>
                <div class="mt-28 max-w-lg">
                    <p class="mb-5 text-xs font-bold uppercase tracking-[0.28em] text-[#b7d084]">Inteligência que move negócios</p>
                    <h1 class="font-aclonica text-5xl leading-[1.12] xl:text-6xl">Comece sua jornada. <span class="text-[#b7d084]">Decida melhor.</span></h1>
                    <p class="mt-7 max-w-md text-base leading-7 text-[#d5ddcf]">Crie sua conta e transforme os dados da sua empresa em decisões mais precisas.</p>
                </div>
            </div>

            <div class="relative z-10 flex items-end justify-between border-t border-white/15 pt-6 text-xs text-[#b8c3b2]">
                <span>Visão para decidir melhor</span>
                <span class="flex items-center gap-2"><i class="h-2 w-2 rounded-full bg-[#b7d084]"></i> Plataforma segura</span>
            </div>
        </section>

        <section class="flex h-screen min-w-0 flex-1 flex-col overflow-hidden px-6 py-5 sm:px-12 sm:py-8 lg:px-16 xl:px-24">
            <div class="flex items-center justify-between lg:justify-end">
                <RouterLink to="/" class="font-aclonica text-xl text-[#52761e] lg:hidden">DataVision</RouterLink>
            </div>

            <div class="mx-auto flex min-h-0 w-full max-w-md flex-1 flex-col justify-center overflow-hidden py-6 sm:py-10">
                <div class="mb-6">
                    <p class="mb-3 text-xs font-bold uppercase tracking-[0.22em] text-[#52761e]">Novo por aqui?</p>
                    <h2 class="font-aclonica text-4xl leading-tight text-[#20251c]">Crie sua conta</h2>
                    <p class="mt-3 text-sm leading-6 text-[#70786b]">Cadastre-se para acessar todo o potencial da DataVision.</p>
                </div>

                <form class="space-y-4" @submit.prevent="cadastrar">
                    <label class="block">
                        <span class="mb-2 block text-sm font-bold text-[#3d4739]">Nome completo</span>
                        <span class="input-wrap flex items-center rounded-xl border border-[#d5dbcf] bg-white px-4 transition focus-within:border-[#52761e] focus-within:ring-4 focus-within:ring-[#52761e]/10">
                            <svg aria-hidden="true" class="mr-3 h-5 w-5 shrink-0 text-[#8a9583]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="8" r="3.5"/><path d="M5 20c.8-3.2 3.1-5 7-5s6.2 1.8 7 5"/></svg>
                            <input v-model="name" required type="text" placeholder="Seu nome completo" class="h-13 w-full bg-transparent text-sm text-[#20251c] outline-none placeholder:text-[#a7afa2]" />
                        </span>
                    </label>

                    <label class="block">
                        <span class="mb-2 block text-sm font-bold text-[#3d4739]">E-mail corporativo</span>
                        <span class="input-wrap flex items-center rounded-xl border border-[#d5dbcf] bg-white px-4 transition focus-within:border-[#52761e] focus-within:ring-4 focus-within:ring-[#52761e]/10">
                            <svg aria-hidden="true" class="mr-3 h-5 w-5 shrink-0 text-[#8a9583]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="3" y="5" width="18" height="14" rx="2"/><path d="m3 7 9 6 9-6"/></svg>
                            <input v-model="email" required type="email" placeholder="voce@empresa.com" class="h-13 w-full bg-transparent text-sm text-[#20251c] outline-none placeholder:text-[#a7afa2]" />
                        </span>
                    </label>

                    <label class="block">
                        <span class="mb-2 block text-sm font-bold text-[#3d4739]">Senha</span>
                        <span class="input-wrap flex items-center rounded-xl border border-[#d5dbcf] bg-white px-4 transition focus-within:border-[#52761e] focus-within:ring-4 focus-within:ring-[#52761e]/10">
                            <svg aria-hidden="true" class="mr-3 h-5 w-5 shrink-0 text-[#8a9583]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="4" y="10" width="16" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></svg>
                            <input v-model="password" required minlength="8" :type="showPassword ? 'text' : 'password'" placeholder="8+ caracteres, letras e números" class="h-13 w-full bg-transparent text-sm text-[#20251c] outline-none placeholder:text-[#a7afa2]" />
                            <button type="button" :aria-label="showPassword ? 'Ocultar senha' : 'Mostrar senha'" class="ml-2 text-[#8a9583] transition hover:text-[#52761e]" @click="showPassword = !showPassword">
                                <svg v-if="!showPassword" aria-hidden="true" class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M2.5 12s3.5-6 9.5-6 9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6Z"/><circle cx="12" cy="12" r="2.5"/></svg>
                                <svg v-else aria-hidden="true" class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="m3 3 18 18M10.6 6.2A10.8 10.8 0 0 1 12 6c6 0 9.5 6 9.5 6a17 17 0 0 1-3.1 3.8M6.2 6.8C3.8 8.4 2.5 12 2.5 12s3.5 6 9.5 6c1.4 0 2.6-.3 3.7-.8"/></svg>
                            </button>
                        </span>
                    </label>

                    <label class="flex cursor-pointer items-start gap-2 pt-1 text-xs leading-5 text-[#6c7566]">
                        <input required type="checkbox" class="mt-1 h-4 w-4 shrink-0 accent-[#52761e]" />
                        <span>Concordo com os termos de uso e a política de privacidade.</span>
                    </label>

                    <p v-if="mensagem" :class="submitted ? 'bg-[#edf4e5] text-[#52761e]' : 'bg-[#fff0ed] text-[#b44d3c]'" class="rounded-lg px-4 py-3 text-center text-sm">{{ mensagem }}</p>
                    <button type="submit" :disabled="loading" class="group flex h-13 w-full items-center justify-center gap-3 rounded-xl bg-[#52761e] px-5 text-sm font-bold text-white shadow-lg shadow-[#52761e]/15 transition hover:bg-[#3f5e15] hover:shadow-xl focus:outline-none focus:ring-4 focus:ring-[#52761e]/20 disabled:cursor-not-allowed disabled:opacity-60">
                        {{ loading ? 'Enviando...' : 'Criar minha conta' }}
                        <svg aria-hidden="true" class="h-4 w-4 transition-transform group-hover:translate-x-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14M13 6l6 6-6 6"/></svg>
                    </button>
                </form>

                <p class="mt-6 text-center text-sm text-[#70786b]">Já possui uma conta?
                    <RouterLink to="/login" class="font-bold text-[#52761e] hover:underline">Entrar agora</RouterLink>
                </p>
                <div class="mt-6 flex items-center gap-4 text-xs text-[#a0a99b] sm:mt-8"><span class="h-px flex-1 bg-[#dfe4da]"></span><span>Seus dados estão protegidos</span><span class="h-px flex-1 bg-[#dfe4da]"></span></div>
            </div>
        </section>
    </main>
</template>

<style scoped>
.brand-grid {
    background-image: linear-gradient(rgba(183, 208, 132, 0.13) 1px, transparent 1px), linear-gradient(90deg, rgba(183, 208, 132, 0.13) 1px, transparent 1px);
    background-size: 44px 44px;
    mask-image: linear-gradient(to bottom, black, transparent 85%);
}
</style>
