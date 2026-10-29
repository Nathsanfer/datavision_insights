import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Dashboard from '../views/Dashboard.vue'
import Upload from '../views/Upload.vue'
import Insights from '../views/Insights.vue'
import Historic from '../views/Historic.vue'
import Profile from '../views/Profile.vue'
import AppLayout from '../layout/AppLayout.vue'

const router  = createRouter({
    history: createWebHistory(),
    routes: [
    { path: '/', component: Home },
    { path: '/login', component: Login },
    { path: '/register', component: Register },
    {
        path: '/',
        component: AppLayout,
        children: [
            { path: 'dashboard', component: Dashboard, meta: { requiresAuth: true } },
            { path: 'upload', component: Upload, meta: { requiresAuth: true } },
            { path: 'insights', component: Insights, meta: { requiresAuth: true } },
            { path: 'historic', component: Historic, meta: { requiresAuth: true } },
            { path: 'profile', component: Profile, meta: { requiresAuth: true } }
        ]
    }
]
})

router.beforeEach((to) => {
    const auth = useAuthStore()
    if (to.meta.requiresAuth && !auth.logado) return '/login'
    if (to.path === '/login' && auth.logado) return '/dashboard'
})

export default router