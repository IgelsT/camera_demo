<template>
  <q-list>
    <q-item-label header class="text-grey-8"
      ><q-icon name="person" />&nbsp;{{ userName }}</q-item-label
    >
    <q-separator />
    <div v-for="item in linksDataActive" :key="item.title">
      <q-item clickable :to="item.link" dense>
        <q-item-section v-if="item.icon" avatar>
          <q-icon :name="item.icon" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ item.title }}</q-item-label>
        </q-item-section>
      </q-item>
    </div>
    <q-item clickable @click="Exit" dense>
      <q-item-section avatar>
        <q-icon name="exit_to_app" />
      </q-item-section>

      <q-item-section>
        <q-item-label>Выйти</q-item-label>
      </q-item-section>
    </q-item>
  </q-list>
</template>

<script setup lang="ts">
name: 'LeftNavBar';

import { useQuasar } from 'quasar';
import { localUserStore } from './../stores/LocalUserStore';

const $q = useQuasar();
const store = localUserStore();
const userName = store.userName;

const linksDataActive = [
  {
    title: 'Сводка',
    icon: 'dashboard',
    link: '/dashboard',
  },
  {
    title: 'Устройства',
    icon: 'phone_android',
    link: '/devices/list',
  },
  {
    title: 'Профиль',
    icon: 'person',
    link: '/userprofile',
  },
];

// const ExpandItem = (item: any) => {
//   let ret = false;
//   const ExpPath = ['/accounts', '/acts', '/payments'];
//   if (ExpPath.includes(this.$route.path)) {
//     ret = true;
//   }
//   return ret;
// };

const Exit = () => {
  $q.dialog({
    title: 'Выйти',
    message: 'Завершить сеанс?',
    cancel: true,
    persistent: true,
  })
    .onOk(() => {
      store.logout('');
    })
    .onCancel(() => {
      // console.log('>>>> Cancel')
    });
};
</script>
