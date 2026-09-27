<script setup>
import { ref } from 'vue'

const isMenuOpen = ref(false)

function closeMenu() {
    isMenuOpen.value = false
}
</script>

<template>
    <header class="border-b border-[#dfe6d8] bg-[#f7f8f3]/95 px-5 py-4 backdrop-blur-md sm:px-8 lg:px-12">
        <div class="mx-auto flex max-w-325 items-center justify-between gap-6">
            <RouterLink to="/dashboard" class="group flex items-center gap-3" @click="closeMenu">
                <span class="grid h-10 w-10 place-items-center rounded-xl bg-[#52761e] shadow-[0_7px_18px_rgba(82,118,30,0.2)] transition-transform group-hover:-rotate-3">
                    <svg aria-hidden="true" class="h-5 w-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M4 19V5M4 19h16" />
                        <path d="m7 15 3-3 3 2 5-6" />
                        <path d="M17 5h3v3" />
                    </svg>
                </span>
                <span>
                    <span class="block font-aclonica text-2xl leading-none text-[#26351f]">DataVision</span>
                    <span class="mt-1 block text-[10px] font-bold uppercase tracking-[0.2em] text-[#84917b]">Insights</span>
                </span>
            </RouterLink>

            <nav class="hidden items-center gap-1 rounded-2xl border border-[#e2e8dd] bg-white/80 p-1.5 shadow-[0_8px_24px_rgba(32,37,28,0.06)] lg:flex" aria-label="Navegação principal">
                <RouterLink to="/dashboard" class="rounded-xl px-4 py-2.5 text-sm font-semibold text-[#6d7768] transition hover:bg-[#f0f5eb] hover:text-[#52761e]" active-class="bg-[#52761e] text-white shadow-sm hover:bg-[#52761e] hover:text-white">Dashboard</RouterLink>
                <RouterLink to="/upload" class="rounded-xl px-4 py-2.5 text-sm font-semibold text-[#6d7768] transition hover:bg-[#f0f5eb] hover:text-[#52761e]" active-class="bg-[#52761e] text-white shadow-sm hover:bg-[#52761e] hover:text-white">Upload</RouterLink>
                <RouterLink to="/insights" class="rounded-xl px-4 py-2.5 text-sm font-semibold text-[#6d7768] transition hover:bg-[#f0f5eb] hover:text-[#52761e]" active-class="bg-[#52761e] text-white shadow-sm hover:bg-[#52761e] hover:text-white">Insights</RouterLink>
                <RouterLink to="/historic" class="rounded-xl px-4 py-2.5 text-sm font-semibold text-[#6d7768] transition hover:bg-[#f0f5eb] hover:text-[#52761e]" active-class="bg-[#52761e] text-white shadow-sm hover:bg-[#52761e] hover:text-white">Histórico</RouterLink>
                <RouterLink to="/profile" class="rounded-xl px-4 py-2.5 text-sm font-semibold text-[#6d7768] transition hover:bg-[#f0f5eb] hover:text-[#52761e]" active-class="bg-[#52761e] text-white shadow-sm hover:bg-[#52761e] hover:text-white">Perfil</RouterLink>
            </nav>

            <div class="hidden items-center gap-4 lg:flex">
                <span class="flex items-center gap-2 text-xs font-semibold text-[#84917b]"><span class="h-2 w-2 rounded-full bg-[#8db84e] shadow-[0_0_0_4px_rgba(141,184,78,0.14)]"></span>Dados seguros</span>
                <RouterLink to="/" class="rounded-xl border border-[#d8e1d1] px-4 py-2.5 text-sm font-bold text-[#52761e] transition hover:border-[#52761e] hover:bg-[#f0f5eb]">Sair</RouterLink>
            </div>

            <button type="button" class="grid h-11 w-11 place-items-center rounded-xl border border-[#d8e1d1] bg-white text-[#52761e] lg:hidden" :aria-expanded="isMenuOpen" aria-controls="mobile-navigation" aria-label="Abrir menu" @click="isMenuOpen = !isMenuOpen">
                <span class="flex w-5 flex-col gap-1.5"><span class="h-0.5 w-full bg-current"></span><span class="h-0.5 w-full bg-current"></span><span class="h-0.5 w-3 bg-current"></span></span>
            </button>
        </div>

        <nav v-if="isMenuOpen" id="mobile-navigation" class="mx-auto mt-4 grid max-w-325 gap-1 border-t border-[#e2e8dd] pt-3 lg:hidden" aria-label="Navegação móvel">
            <RouterLink v-for="item in [{ label: 'Dashboard', path: '/dashboard' }, { label: 'Upload', path: '/upload' }, { label: 'Insights', path: '/insights' }, { label: 'Histórico', path: '/historic' }, { label: 'Perfil', path: '/profile' }]" :key="item.path" :to="item.path" class="rounded-xl px-4 py-3 text-sm font-semibold text-[#6d7768] transition hover:bg-[#f0f5eb]" active-class="bg-[#52761e] text-white" @click="closeMenu">{{ item.label }}</RouterLink>
            <RouterLink to="/" class="mt-2 rounded-xl border border-[#d8e1d1] px-4 py-3 text-sm font-bold text-[#52761e]" @click="closeMenu">Sair</RouterLink>
        </nav>
    </header>
</template>