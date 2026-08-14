<script setup>
import router from '../router'
</script>

<template>
  <header class="flex flex-row justify-between h-20 items-center px-20">
    <h1 class="font-aclonica text-2xl" style="color: #52761E;">DataVision</h1>
    <nav class="space-x-20">
      <a href="#desafio">Desafio</a>
      <a href="#demostracao">Demonstração</a>
      <a href="#planos">Planos</a>
      <a href="#faq">FAQ</a>
    </nav>
    <button><Router-link to="/login" class="border-2 border-[#52761E] text-[#52761E] hover:bg-[#52761E] hover:text-white font-bold py-2 px-4 rounded-xl transition-colors duration-200">Acesse o Sistema</Router-link></button>
  </header>

  <main>
    <section class="h-160 bg-amber-100"></section>

    <section id="desafio" class="h-100 bg-amber-200">
      <h1 class="font-aclonica">Desafio</h1>
    </section>

    <section id="demostracao" class="h-100 bg-amber-300">
      <h1 class="font-aclonica">Demonstração</h1>
    </section>
  </main>
</template>
