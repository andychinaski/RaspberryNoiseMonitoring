<script setup>
import AppMenuItem from './AppMenuItem.vue';

const model = [
    {
        label: 'Разделы',
        items: [
            { label: 'Мониторинг', icon: 'pi pi-fw pi-chart-line', to: '/' },
            { label: 'История', icon: 'pi pi-fw pi-history', to: '/history' },
            { label: 'Уведомления', icon: 'pi pi-fw pi-bell', to: '/notifications' },
            { label: 'Устройство', icon: 'pi pi-fw pi-desktop', to: '/device' }
        ]
    }
];
</script>

<template>
    <ul class="layout-menu">
        <app-menu-item v-for="(item, index) in model" :key="item.label" :item="item" :index="index" />
    </ul>
</template>
