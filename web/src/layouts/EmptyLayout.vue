import { defineComponent } from "vue";

<template>
  <q-layout>
    <q-page-container>
      <router-view />
    </q-page-container>
    <!-- <SnackBar /> -->
  </q-layout>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
// import SnackBar from '../components/SnackBar.vue';

export default defineComponent({
  name: 'EmptyLayout',
  // components: { SnackBar },
});
</script>
