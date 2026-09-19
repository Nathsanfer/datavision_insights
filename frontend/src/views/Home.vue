<script setup>
import { ref } from "vue";
import Footer from "../components/Footer.vue";

const openFaq = ref(null);

const problemItems = [
  {
    icon: "▣",
    title: "Planilhas dispersas e desatualizadas",
    description: "Dificuldades para consolidar e confiar em dados.",
  },
  {
    icon: "↻",
    title: "Processos manuais e demorados",
    description: "Retrabalho, erros e perda de tempo da equipe.",
  },
  {
    icon: "◉",
    title: "Falta de visão estratégica",
    description: "Sem análises profundas, oportunidades passam despercebidas.",
  },
  {
    icon: "◆",
    title: "Dificuldades para tomar decisões",
    description: "Decisões baseadas em achismos, não em dados.",
  },
];

const solutionItems = [
  {
    icon: "≡",
    title: "Dados centralizados e confiáveis",
    description: "Integração e padronização das informações.",
  },
  {
    icon: "▤",
    title: "Análises automáticas e precisas",
    description: "Relatórios e indicadores atualizados em tempo real.",
  },
  {
    icon: "▥",
    title: "Insights que geram oportunidades",
    description: "Identifiquem padrões, perfil de clientes e serviços mais contratados.",
  },
  {
    icon: "⌁",
    title: "Decisões com segurança",
    description: "Informação certa para o crescimento do negócio.",
  },
];

const plans = [
  {
    name: "Básico",
    description: "Ideal para pequenas equipes que estão começando.",
    price: "R$ 99",
    period: "/mês",
    features: [
      "Até 5 usuários",
      "Dashboards padrão",
      "Relatórios básicos",
      "Suporte por e-mail",
    ],
    action: "Começar agora",
  },
  {
    name: "Profissional",
    description: "Para empresas que querem mais análises e insights.",
    price: "R$ 199",
    period: "/mês",
    features: [
      "Até 20 usuários",
      "Dashboards personalizados",
      "Relatórios avançados",
      "Filtros e segmentações",
      "Suporte prioritário",
    ],
    action: "Começar agora",
    featured: true,
  },
  {
    name: "Enterprise",
    description: "Para grandes empresas com demandas específicas.",
    price: "Sob consulta",
    period: "",
    features: [
      "Usuários ilimitados",
      "Dashboards ilimitados",
      "Integrações via API",
      "Suporte dedicado",
      "Treinamento e onboarding",
    ],
    action: "Fale com especialista",
  },
];

const planHighlights = [
  { icon: "▣", text: "Pagamento seguro" },
  { icon: "◌", text: "Cancele quando quiser" },
  { icon: "♧", text: "Sem fidelidade" },
  { icon: "☻", text: "Suporte humanizado" },
];

const faqItems = [
  {
    question: "O que é a DataVision?",
    answer: "A DataVision organiza seus dados e transforma informações espalhadas em insights claros para apoiar decisões mais estratégicas.",
  },
  {
    question: "Preciso ter conhecimento técnico para usar a plataforma?",
    answer: "Não. A plataforma foi pensada para ser simples e intuitiva, com dashboards e relatórios que sua equipe consegue interpretar com facilidade.",
  },
  {
    question: "Posso cancelar meu plano quando quiser?",
    answer: "Sim. Nossos planos não têm fidelidade e podem ser cancelados quando você quiser, sem complicação.",
  },
  {
    question: "A DataVision oferece suporte?",
    answer: "Sim. Todos os planos contam com suporte, e as opções Profissional e Enterprise têm atendimento prioritário e dedicado.",
  },
  {
    question: "Meus dados ficam seguros?",
    answer: "Sim. Utilizamos boas práticas de segurança e controle de acesso para manter suas informações protegidas.",
  },
];

function toggleFaq(index) {
  openFaq.value = openFaq.value === index ? null : index;
}
</script>

<template>
  <header class="sticky top-0 z-20 border-b border-[#e5e9e1]/80 bg-[#f7f8f3]/90 backdrop-blur-md">
    <div class="mx-auto flex h-20 max-w-300 items-center justify-between px-5 sm:px-8 lg:px-12">
    <a href="#top" class="font-aclonica text-2xl tracking-wide text-[#52761e]">DataVision</a>
    <nav class="hidden items-center gap-8 text-sm text-[#555] md:flex">
      <a href="#desafio" class="transition-colors hover:text-[#52761e]">Desafio</a>
      <a href="#planos" class="transition-colors hover:text-[#52761e]">Planos</a>
      <a href="#faq" class="transition-colors hover:text-[#52761e]">FAQ</a>
    </nav>
    <RouterLink to="/login" class="group flex items-center gap-2 rounded-xl bg-[#52761e] px-4 py-2.5 text-sm font-bold text-white shadow-lg shadow-[#52761e]/15 transition hover:bg-[#3f5e15]">Acesse o sistema <span aria-hidden="true" class="transition-transform group-hover:translate-x-0.5">↗</span></RouterLink>
    </div>
  </header>

  <main id="top">
    <section class="hero-shell relative overflow-hidden bg-[#26351f] text-white">
      <div class="hero-grid absolute inset-0 opacity-30"></div>
      <div class="absolute -right-32 -top-28 h-96 w-96 rounded-full border border-[#b7d084]/20"></div>
      <div class="absolute -right-10 top-16 h-64 w-64 rounded-full border border-[#b7d084]/15"></div>
      <div class="relative mx-auto grid max-w-300 items-center gap-12 px-5 py-16 sm:px-8 sm:py-20 lg:grid-cols-[0.9fr_1.1fr] lg:px-12 lg:py-24">
        <div class="max-w-xl">
          <p class="mb-5 text-xs font-bold uppercase tracking-[0.28em] text-[#b7d084]">Inteligência que move negócios</p>
          <h1 class="font-aclonica text-4xl leading-[1.12] sm:text-5xl xl:text-6xl">Transformamos dados em <span class="text-[#b7d084]">decisões inteligentes.</span></h1>
          <p class="mt-7 max-w-lg text-base leading-7 text-[#d5ddcf]">Organize, analise e visualize as informações importantes para entender seu negócio, identificar oportunidades e decidir com mais assertividade.</p>
          <div class="mt-9 flex flex-col gap-3 sm:flex-row">
            <a href="#desafio" class="group inline-flex h-13 items-center justify-center gap-3 rounded-xl bg-[#b7d084] px-6 text-sm font-bold text-[#26351f] shadow-lg shadow-black/10 transition hover:-translate-y-0.5 hover:bg-white">Explorar soluções <span aria-hidden="true" class="text-lg transition-transform group-hover:translate-x-1">→</span></a>
            <RouterLink to="/login" class="inline-flex h-13 items-center justify-center rounded-xl border border-white/25 px-6 text-sm font-bold text-white transition hover:border-[#b7d084] hover:text-[#b7d084]">Ver meu painel</RouterLink>
          </div>
          <div class="mt-12 grid max-w-md grid-cols-3 gap-5 border-t border-white/15 pt-5">
            <div><strong class="block font-aclonica text-xl text-[#b7d084]">24/7</strong><span class="text-xs text-[#b8c3b2]">Visão dos dados</span></div>
            <div><strong class="block font-aclonica text-xl text-[#b7d084]">100%</strong><span class="text-xs text-[#b8c3b2]">Mais clareza</span></div>
            <div><strong class="block font-aclonica text-xl text-[#b7d084]">1 só</strong><span class="text-xs text-[#b8c3b2]">Plataforma</span></div>
          </div>
        </div>
        <div class="relative mx-auto w-full max-w-2xl lg:pl-6">
          <div class="absolute -inset-4 rounded-[2rem] border border-[#b7d084]/20"></div>
          <div class="relative overflow-hidden rounded-2xl border border-white/15 bg-[#f7f8f3] p-3 shadow-2xl shadow-black/25"><img src="../assets/hero.png" alt="Dashboard de análise de dados da DataVision" class="aspect-[1.25] w-full rounded-xl object-cover" /></div>
          <div class="absolute -bottom-5 -left-3 flex items-center gap-3 rounded-xl border border-[#dfe8d1] bg-white px-4 py-3 text-[#26351f] shadow-xl sm:left-0"><span class="grid h-9 w-9 place-items-center rounded-lg bg-[#edf4e5] text-lg text-[#52761e]">↗</span><span><strong class="block text-sm">Decisões melhores</strong><small class="text-xs text-[#788274]">começam com dados claros</small></span></div>
        </div>
      </div>
    </section>
    <div class="h-2 bg-[#b7d084]"></div>

    <!-- Problem and Solution Section -->
    <section id="desafio" class="scroll-mt-20 bg-white px-4 py-20 sm:px-8 lg:px-12">
      <div class="mx-auto flex max-w-260 flex-col items-center">
        <div class="mb-12 text-center">
          <span class="text-xs font-bold uppercase tracking-[0.18em] text-[#52761e]">O que muda</span>
          <h2 class="mt-3 font-aclonica text-4xl sm:text-5xl">Do problema à solução</h2>
          <p class="mt-3 text-sm text-gray-500 sm:text-base">
            Entenda os desafios que a sua empresa enfrenta e como o DataVision resolve.
          </p>
        </div>

        <div class="flex w-full flex-col items-stretch justify-center lg:flex-row lg:items-center">
          <article class="w-full min-h-[31rem] max-w-[31rem] rounded-2xl bg-[#f5f7f2] px-7 py-6 shadow-[0_12px_35px_rgba(38,53,31,0.05)] transition-transform duration-300 hover:-translate-y-1 lg:rounded-r-none">
            <span class="inline-flex items-center gap-[0.3rem] rounded-full border border-[#52761e] px-4 py-[0.3rem] text-xs text-[#777]"><span aria-hidden="true" class="text-base text-[#52761e]">○</span> O PROBLEMA</span>
            <h3 class="mt-6 max-w-[29rem] font-aclonica text-[clamp(1.35rem,2vw,1.65rem)] leading-[1.15]">Dados desconectados, decisões no escuro</h3>
            <p class="mt-4 text-sm leading-[1.6] text-[#888]">
              Informações espalhadas em planilhas, processos manuais, e falta de visibilidade impedem sua empresa de enxergar o que realmente importa.
            </p>
            <ul class="mt-5 grid gap-3.5">
              <li v-for="item in problemItems" :key="item.title">
                <span class="flex items-start gap-3.5 text-xs leading-[1.45] text-[#777]"><span class="grid h-9 w-9 flex-none place-items-center rounded-[0.35rem] bg-[#eef0ec] text-[1.45rem] font-bold text-[#52761e]" aria-hidden="true">{{ item.icon }}</span><span><strong class="block font-medium text-[#707070]">{{ item.title }}</strong><small class="block text-inherit">{{ item.description }}</small></span></span>
              </li>
            </ul>
          </article>

          <img
            src="../assets/icons/circle.png"
            alt="Círculo listrado conectando problema e solução"
            class="z-[1] my-[-1rem] h-32 w-32 object-contain lg:mx-0"
          />

          <article class="w-full min-h-[31rem] max-w-[31rem] rounded-2xl bg-[#f5f7f2] px-7 py-6 shadow-[0_12px_35px_rgba(38,53,31,0.05)] transition-transform duration-300 hover:-translate-y-1 lg:rounded-l-none">
            <span class="inline-flex items-center gap-[0.3rem] rounded-full border border-[#52761e] px-4 py-[0.3rem] text-xs text-[#777]"><span aria-hidden="true" class="text-base text-[#52761e]">○</span> A SOLUÇÃO</span>
            <h3 class="mt-6 max-w-[29rem] font-aclonica text-[clamp(1.35rem,2vw,1.65rem)] leading-[1.15]">Inteligência de dados para impulsionar resultados</h3>
            <p class="mt-4 text-sm leading-[1.6] text-[#888]">
              O DataVision centraliza, organiza e transforma seus dados em informações estratégicas, gerando clareza e base sólida para decisões mais assertivas.
            </p>
            <ul class="mt-5 grid gap-3.5">
              <li v-for="item in solutionItems" :key="item.title">
                <span class="flex items-start gap-3.5 text-xs leading-[1.45] text-[#777]"><span class="grid h-9 w-9 flex-none place-items-center rounded-[0.35rem] bg-[#eef0ec] text-[1.45rem] font-bold text-[#52761e]" aria-hidden="true">{{ item.icon }}</span><span><strong class="block font-medium text-[#707070]">{{ item.title }}</strong><small class="block text-inherit">{{ item.description }}</small></span></span>
              </li>
            </ul>
          </article>
        </div>
      </div>
    </section>

    <section id="planos" class="scroll-mt-20 bg-[#f7f8f3] px-5 py-20 sm:px-8 lg:px-16">
      <div class="mx-auto max-w-270 text-center">
        <span class="text-xs font-bold uppercase tracking-[0.18em] text-[#52761e]">Planos simples e transparentes</span>
        <h2 class="font-aclonica text-4xl leading-tight sm:text-5xl">Planos que se adaptam ao seu negócio</h2>
        <p class="mt-3 text-sm text-[#777] sm:text-base">Escolha o plano ideal para transformar os dados da sua empresa.</p>

        <div class="mt-10 grid items-stretch gap-7 lg:grid-cols-3 lg:gap-10">
          <article
            v-for="plan in plans"
            :key="plan.name"
            class="relative flex min-h-[25.6rem] flex-col overflow-hidden rounded-2xl border border-[#edf0ea] bg-white px-6 pb-5 pt-10 text-left shadow-[0_8px_24px_rgba(38,53,31,0.06)] transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_16px_32px_rgba(38,53,31,0.1)]"
            :class="plan.featured ? 'pt-14' : ''"
          >
            <div v-if="plan.featured" class="absolute inset-x-0 top-0 bg-[#52761e] py-1 text-center text-sm text-white">
              MAIS ESCOLHIDO
            </div>
            <h3 class="text-center text-2xl text-[#52761e]">{{ plan.name }}</h3>
            <p class="mx-auto mt-3 max-w-[15rem] text-center text-sm leading-relaxed text-[#888]">{{ plan.description }}</p>
            <p class="mt-5 text-center text-[1.7rem] text-[#333]">
              {{ plan.price }} <span v-if="plan.period" class="text-base text-[#999]">{{ plan.period }}</span>
            </p>
            <ul class="mt-5 grid gap-1.5 text-sm text-[#777]">
              <li v-for="feature in plan.features" :key="feature" class="flex items-center gap-2">
                <span class="text-lg leading-none text-[#52761e]" aria-hidden="true">✓</span>
                <span>{{ feature }}</span>
              </li>
            </ul>
            <button
              class="mt-auto h-10 rounded-xl border border-[#52761e] px-4 text-sm font-medium text-[#52761e] transition-colors duration-200 hover:bg-[#52761e] hover:text-white"
              :class="plan.featured ? 'bg-[#52761e] text-white hover:bg-[#3f5d17]' : ''"
            >
              {{ plan.action }}
            </button>
          </article>
        </div>

        <div class="mt-8 grid gap-5 text-sm text-[#777] sm:grid-cols-2 lg:grid-cols-4">
          <div v-for="highlight in planHighlights" :key="highlight.text" class="flex items-center justify-center gap-2">
            <span class="text-xl text-[#666]" aria-hidden="true">{{ highlight.icon }}</span>
            <span>{{ highlight.text }}</span>
          </div>
        </div>
      </div>
    </section>

    <section id="faq" class="scroll-mt-20 bg-white px-5 py-20 sm:px-8 lg:px-16">
      <div class="mx-auto max-w-190">
        <div class="text-center">
          <span class="text-xs font-bold uppercase tracking-[0.18em] text-[#52761e]">Estamos aqui para ajudar</span>
          <h2 class="font-aclonica text-4xl leading-tight sm:text-5xl">Perguntas frequentes</h2>
          <p class="mt-3 text-sm text-[#777] sm:text-base">Tudo o que você precisa saber para começar.</p>
        </div>

        <div class="mt-10 divide-y divide-[#e5e9e1] rounded-2xl border border-[#e5e9e1] bg-[#f7f8f3] px-6 shadow-[0_10px_30px_rgba(38,53,31,0.04)]">
          <div v-for="(item, index) in faqItems" :key="item.question" class="py-1">
            <button
              type="button"
              class="flex w-full items-center justify-between gap-6 py-5 text-left text-base font-medium text-[#333] transition-colors hover:text-[#52761e]"
              :aria-expanded="openFaq === index"
              @click="toggleFaq(index)"
            >
              <span>{{ item.question }}</span>
              <span class="grid h-7 w-7 flex-none place-items-center rounded-full border border-[#52761e] text-lg font-normal leading-none text-[#52761e]" aria-hidden="true">
                {{ openFaq === index ? "−" : "+" }}
              </span>
            </button>
            <p v-if="openFaq === index" class="max-w-165 pb-5 pr-12 text-sm leading-relaxed text-[#777]">
              {{ item.answer }}
            </p>
          </div>
        </div>
      </div>
    </section>
  </main>

  <Footer />
</template>
