<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar>
        <q-btn flat dense round icon="menu" aria-label="Menu" @click="leftDrawerOpen = !leftDrawerOpen" />
        <q-space></q-space>
        <!-- <q-btn flat dense round icon="phone" @click="SipPhoneOptions.PhoneIsVisible = !SipPhoneOptions.PhoneIsVisible" /> -->
        <!-- <q-icon name="person" />
        <span>{{ UserName }}</span> -->
        <q-btn-dropdown v-if="showChangeView()" color="primary" dense flat text-color="white" icon="grid_on">
          <q-list>
            <q-item clickable v-close-popup @click="viewChange(1)">
              <q-item-section>
                <q-item-label>1 столбец</q-item-label>
              </q-item-section>
            </q-item>
            <q-item clickable v-close-popup @click="viewChange(2)">
              <q-item-section>
                <q-item-label>2 столбца</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>
      </q-toolbar>
    </q-header>

    <q-drawer v-model="leftDrawerOpen" show-if-above bordered content-class="bg-grey-1">
      <NavLink />
    </q-drawer>
    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
name: 'MainLayout';

import { ref } from 'vue';
import NavLink from 'src/components/NavLink.vue';
import { dashBoardStore } from 'src/stores/DashBoardStore';
import { useRoute } from 'vue-router';

components: {
  NavLink;
}

const dashStore = dashBoardStore();
const leftDrawerOpen = ref(false);
const route = useRoute();

const viewChange = (cols: number) => {
  dashStore.mobileViewCols = cols;
};

const showChangeView = () => {
  return dashStore.isMobileDevice && route.name == 'dashboard';
};
</script>
