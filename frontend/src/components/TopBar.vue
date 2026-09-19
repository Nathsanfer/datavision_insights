<template>
    <div class="h-24 flex items-center justify-between px-20">
        <h2 class="font-aclonica text-3xl" style="color: #52761e">DataVision</h2>
        <nav class="ml-16 flex flex-none items-center rounded-full bg-white text-black shadow-[0_8px_24px_rgba(32,37,28,0.08)]">
            <ul class="flex items-center justify-center gap-12 text-base">
                <li><RouterLink to="/dashboard" class="inline-block rounded-full px-7 py-3 transition-colors" active-class="bg-[#52761E] text-white">Dashboard</RouterLink></li>
                <li><RouterLink to="/upload" class="inline-block rounded-full px-5 py-3 transition-colors" active-class="bg-[#52761E] text-white">Upload</RouterLink></li>
                <li><RouterLink to="/insights" class="inline-block rounded-full px-5 py-3 transition-colors" active-class="bg-[#52761E] text-white">Insights</RouterLink></li>
                <li><RouterLink to="/historic" class="inline-block rounded-full px-5 py-3 transition-colors" active-class="bg-[#52761E] text-white">Histórico</RouterLink></li>
                <li><RouterLink to="/profile" class="inline-block rounded-full px-5 py-3 transition-colors" active-class="bg-[#52761E] text-white">Perfil</RouterLink></li>
                <li><a class="inline-block rounded-full px-5 py-3 transition-colors" href="/logout">Sair</a></li>
            </ul>
        </nav>
    </div>
</template>