import { createRouter, createWebHistory } from 'vue-router'

import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Dashboard from '../views/Dashboard.vue'
import Upload from '../views/Upload.vue'
import Insights from '../views/Insights.vue'
import Historic from '../views/Historic.vue'
import Profile from '../views/Profile.vue'
import AppLayout from '../layout/AppLayout.vue'

const routes = [
    { path: '/', component: Home },
    { path: '/login', component: Login },
    { path: '/register', component: Register },
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