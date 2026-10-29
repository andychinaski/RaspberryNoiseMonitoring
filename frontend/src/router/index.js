import AppLayout from '@/layout/AppLayout.vue';
import { createRouter, createWebHistory } from 'vue-router';

export default createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            component: AppLayout,
            children: [
                { path: '', name: 'home', component: () => import('@/views/Home.vue') },
                { path: 'history', name: 'history', component: () => import('@/views/History.vue') },
                { path: 'notifications', name: 'notifications', component: () => import('@/views/Notifications.vue') },
                { path: 'device', name: 'device', component: () => import('@/views/Device.vue') }
            ]
        }
    ]
});
