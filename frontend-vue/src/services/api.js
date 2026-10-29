import axios from 'axios';

// Armazenamento da URL da API 

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8081',
    headers: { 'Content-Type': 'application/json' }
})

// Busca o token e adiciona no header da requisição

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
})

export default api;