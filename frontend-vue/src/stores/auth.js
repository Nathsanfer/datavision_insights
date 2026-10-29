import { defineStore } from 'pinia';
import api from '../services/api';

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('token'),
        usuario: JSON.parse(localStorage.getItem('usuario') || 'null'),
    }),

    getters: {
        logado: (state) => !!state.token,
        isAdmin: (state) => state.usuario?.perfil === 'ADMIN',
    },

    actions: {
        async login(email, senha) {
            const data = await api.post('/api/auth/login', { email, senha });
            this.token = data.data.token;
            this.usuario = data.data.usuario;
            localStorage.setItem('token', this.token);
            localStorage.setItem('usuario', JSON.stringify(this.usuario));
        },
        // Guarda a sessão devolvida pelo backend (ex.: após alterar o próprio e-mail, que gera novo token).
        salvarSessao(data) {
            this.token = data.token;
            this.usuario = data.usuario;
            localStorage.setItem('token', this.token);
            localStorage.setItem('usuario', JSON.stringify(this.usuario));
        },
        logout() {
            this.token = null; this.usuario = null;
            localStorage.removeItem('token'); localStorage.removeItem('usuario');
        }

    }
})