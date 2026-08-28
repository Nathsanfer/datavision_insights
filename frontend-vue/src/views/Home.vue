<script setup>
import router from "../router";
</script>

<template>
  <!-- Header -->
  <header class="flex flex-row justify-between h-20 items-center px-20">
    <h2 class="font-aclonica text-2xl" style="color: #52761e">DataVision</h2>
    <nav class="space-x-20">
      <a href="#desafio">Desafio</a>
      <a href="#demostracao">Demonstração</a>
      <a href="#planos">Planos</a>
      <a href="#faq">FAQ</a>
    </nav>
    <button>
      <Router-link
        to="/login"
        class="border-2 border-[#52761E] text-[#52761E] hover:bg-[#52761E] hover:text-white font-bold py-2 px-4 rounded-xl transition-colors duration-200"
      >
        Acesse o Sistema
      </Router-link>
    </button>
  </header>

  <main>
    <!-- Hero Section -->
    <section class="h-160 flex">
      <div class="w-45 flex flex-col mt-33 items-center">
        <div class="h-40 w-0.5 bg-gray-500 rounded-2xl"></div>
        <img
          src="../assets/icons/bar_chart.png"
          alt="Gráfico de barras"
          class="w-5 h-5 mt-5"
        />
        <img
          src="../assets/icons/pie_chart.png"
          alt="Gráfico de pizza"
          class="w-5 h-5 mt-5"
        />
        <img src="../assets/icons/data.png" alt="Dados" class="w-5 h-5 mt-5" />
      </div>
      <div class="w-170 px-10 flex flex-col justify-center">
        <h1 class="font-aclonica text-[55px]">Transformamos</h1>
        <h1 class="font-aclonica text-[55px]" style="color: #52761e">
          dados em decisões
        </h1>
        <h1 class="font-aclonica text-[55px]">inteligentes</h1>
        <p class="mt-6 w-113">
          Organize, análise e visualize informações importantes para entender
          seu negócio, identificar oportunidades e tomar decisões com mais
          assertividade.
        </p>
        <button
          class="mt-10 w-60 h-12 border-2 border-[#52761E] text-gray-600 font-bold py-2 px-4 rounded-xl hover:bg-green-700 transition-colors duration-200"
        >
          Explorar Soluções
          <img
            src="../assets/icons/arrow.png"
            alt="Seta para direita"
            class="w-4 h-4 ml-2 inline-block"
          />
        </button>
      </div>

      <div class="w-1/2">
        <img
          src="../assets/hero.png"
          alt="Imagem de um dashboard"
          class="w-160 h-150 object-cover ml-10"
        />
      </div>
    </section>

    <!-- Divisor -->
    <div class="h-10 bg-[#52761E] "></div>

    <!-- Problem and Solution Section -->
    <section id="desafio" class="h-170 mt-10 bg-while flex flex-col justify-center">
        <div class="h-30 flex justify-center items-center flex-col">
        <h2 class="font-aclonica text-[50px]">Do problema à solução </h2>
        <p>Entenda os desafios que a sua empresa enfrenta e como o DataVision resolve.</p>
        </div>
        <div class="h-140 relative flex justify-center items-center">
        <div class="h-130 w-130 rounded-xl" style="background-color: rgba(82, 118, 30, 0.06);">
            <div class="border-2 border-[#52761E] w-32 flex justify-center items-center p-1 rounded-xl mt-5 ml-5"><p class="text-[13px]">O PROBLEMA</p></div>
            <h3 class="text-[25px] w-118 font-semibold ml-5 mt-5">Dados desconectados, decisões no escuro</h3>
            <p class="w-118 ml-5 mt-4">Informações espalhadas em planilhas, processos manuais, e falta de visiabilidade impedem sua empresa de enxergar o que realmente importa.</p>
        </div>
        <img
          src="../assets/icons/circle.png"
          alt="Circulo listrado"
          class="relative z-10 w-50 h-50 -mx-6 -mt-8"
        />
        <div class="h-130 w-130 rounded-xl" style="background-color: rgba(82, 118, 30, 0.06);">
            <div class="border-2 border-[#52761E] w-32 flex justify-center items-center p-1 rounded-xl mt-5 ml-5"><p class="text-[13px]">A SOLUÇÃO</p></div>
            <h3 class="text-[25px] w-118 font-semibold ml-5 mt-5">Inteligência de dados para impulsionar resultados</h3>
            <p class="w-118 ml-5 mt-4">O DataVision centraliza, organiza e transforma seus dados em informações estratégicas, gerando clareza e base sólida para decisões mais assertivas.</p>
        </div>
        </div>
    </section>

    <section id="demostracao" class="h-100 bg-while">
      <h1 class="font-aclonica">Demonstração</h1>
    </section>
  </main>
</template>
