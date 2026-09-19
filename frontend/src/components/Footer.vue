<template>
  <footer class="bg-[#26351f] px-6 py-14 text-white sm:px-10 lg:px-20">
    <div class="mx-auto grid max-w-270 gap-10 lg:grid-cols-[1.5fr_1fr_1fr_1.2fr]">
      <div>
        <h2 class="font-aclonica text-2xl text-[#b7d887]">DataVision</h2>
        <p class="mt-4 max-w-xs text-sm leading-7 text-[#d4d9cf]">
          Dados mais claros para decisões mais inteligentes e resultados que fazem diferença.
        </p>
      </div>

      <div>
        <h3 class="text-sm font-bold uppercase tracking-[0.12em] text-[#b7d887]">Navegação</h3>
        <nav class="mt-4 grid gap-3 text-sm text-[#d4d9cf]">
          <a href="#desafio" class="transition-colors hover:text-white">Desafio</a>
          <a href="#planos" class="transition-colors hover:text-white">Planos</a>
          <a href="#faq" class="transition-colors hover:text-white">FAQ</a>
        </nav>
      </div>

      <div>
        <h3 class="text-sm font-bold uppercase tracking-[0.12em] text-[#b7d887]">Contato</h3>
        <div class="mt-4 grid gap-3 text-sm text-[#d4d9cf]">
          <a href="mailto:contato@datavision.com" class="transition-colors hover:text-white">contato@datavision.com</a>
          <span>Segunda a sexta, 8h às 18h</span>
        </div>
      </div>

      <div>
        <h3 class="text-sm font-bold uppercase tracking-[0.12em] text-[#b7d887]">Comece agora</h3>
        <p class="mt-4 text-sm leading-7 text-[#d4d9cf]">Tenha uma visão mais estratégica do seu negócio.</p>
        <RouterLink to="/login" class="mt-5 inline-flex rounded-xl bg-[#b7d887] px-5 py-2.5 text-sm font-bold text-[#26351f] transition-all duration-200 hover:-translate-y-0.5 hover:bg-white">
          Acesse o sistema
        </RouterLink>
      </div>
    </div>

    <div class="mx-auto mt-10 flex max-w-270 flex-col gap-3 border-t border-white/15 pt-5 text-xs text-[#b5beb0] sm:flex-row sm:items-center sm:justify-between">
      <span>© 2026 DataVision. Todos os direitos reservados.</span>
      <span>Informação certa para decisões melhores.</span>
    </div>
  </footer>
</template>
