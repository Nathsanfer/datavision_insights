<template>
    <header class="sticky top-0 z-20 border-b border-[#e5e9e1]/80 bg-[#f7f8f3]/90 backdrop-blur-md">
        <div class="mx-auto flex h-20 max-w-350 items-center justify-between px-5 sm:px-8 lg:px-12">
            <a href="#top" class="font-aclonica text-2xl tracking-wide text-[#52761e]">DataVision</a>
            <div class="flex items-center gap-3">
                <RouterLink to="/login"
                    class="group flex items-center gap-2 rounded-xl bg-[#52761e] px-4 py-2.5 text-sm font-bold text-white shadow-lg shadow-[#52761e]/15 transition hover:bg-[#3f5e15]">
                    Acesse o sistema <span aria-hidden="true"
                        class="transition-transform group-hover:translate-x-0.5">↗</span></RouterLink>
                <RouterLink to="/register"
                    class="group flex items-center gap-2 rounded-xl bg-[#f7f8f3] border-2 border-[#52761e] px-4 py-2.5 text-sm font-bold text-[#52761e] shadow-lg shadow-[#52761e]/15 transition hover:bg-[#51761e30]">
                    Cadastre-se <span aria-hidden="true"
                        class="transition-transform group-hover:translate-x-0.5">↗</span></RouterLink>
            </div>
        </div>
    </header>
</template>