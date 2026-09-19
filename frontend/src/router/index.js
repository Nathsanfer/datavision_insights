import { createRouter, createWebHistory } from 'vue-router'

// Importando as views para as rotas
import Login from "../views/Login.vue"
import Home from "../views/Home.vue"
import Dashboard from "../views/Dashboard.vue"
import Upload from "../views/Upload.vue"
import Insights from "../views/Insigths.vue"
import Historic from "../views/Historic.vue"
import Profile from "../views/Profile.vue"
import Teste from "../views/Teste.vue"
import AppLayout from "../layout/AppLayout.vue"

// Define as rotas
const routes = [
    { path : '/', component: Home },
    { path : '/login', component: Login },
    { path : '/teste', component: Teste },
    {
        path: '/',
        component: AppLayout,
        children: [
            { path: 'dashboard', component: Dashboard },
            { path: 'upload', component: Upload },
            { path: 'insights', component: Insights },
            { path: 'historic', component: Historic },
            { path: 'profile', component: Profile }
        ]
    }
]

const router  = createRouter({
    history: createWebHistory(),
    routes
})

export default router