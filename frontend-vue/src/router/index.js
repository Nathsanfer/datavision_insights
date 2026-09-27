import { createRouter, createWebHistory } from 'vue-router'

// Importa as views

import Home from '../views/Home.vue'

// Define as rotas

const routes = [
    { path: '/', component: Home }
]

const router  = createRouter({
    history: createWebHistory(),
    routes
})

export default router