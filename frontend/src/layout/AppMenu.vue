<script setup>
import AppMenuItem from './AppMenuItem.vue';

const model = [{ label: 'Главная', items: [{ label: 'Главная', icon: 'pi pi-fw pi-home', to: '/' }] }];
</script>

<template>
    <ul class="layout-menu">
        <app-menu-item v-for="(item, index) in model" :key="item.label" :item="item" :index="index" />
    </ul>
</template>
