<template>
  <router-view />
  <SnackBar />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import SnackBar from 'src/components/SnackBar.vue';

export default defineComponent({
  name: 'App',
  components: { SnackBar },
});
</script>
