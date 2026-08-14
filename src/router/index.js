import { createRouter, createWebHistory } from 'vue-router'

// Importando as views para as rotas
import Login from "../views/Login.vue"
import Home from "../views/Home.vue"

// Define as rotas
const routes = [
    { path : '/', component: Home },
    { path : '/login', component: Login },
]

const router  = createRouter({
    history: createWebHistory(),
    routes
})

export default router