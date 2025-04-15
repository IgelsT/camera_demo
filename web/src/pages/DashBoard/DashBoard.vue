<template>
  <div v-if="!isMobileDevice">
    <DescktopComp />
  </div>
  <div v-if="isMobileDevice">
    <MobileComp />
  </div>
</template>

<script lang="ts">
import { isMobile } from 'src/common/utils';
import { ref, defineComponent } from 'vue';
import DescktopComp from './DesktopComp.vue';
import MobileComp from './MobileComp.vue';
import { dashBoardStore } from 'src/stores/DashBoardStore';

const isLoaded = ref(false);
const camList = ref([{}]);
const isMobileDevice = ref(false);
const store = dashBoardStore();

function checkModile() {
  if (isMobile.any() != null) {
    isMobileDevice.value = true;
    store.isMobileDevice = true;
  } else {
    isMobileDevice.value = false;
    store.isMobileDevice = false;
  }
}

export default defineComponent({
  name: 'DashBoard',

  components: {
    DescktopComp,
    MobileComp,
  },

  computed: {
    isMobile(): boolean {
      console.log(isMobile.any());
      if (isMobile.any() != null) return true;
      return false;
    },
  },

  setup() {
    // const { width } = useWindowSize();
    checkModile();
    // getData();

    return {
      isLoaded,
      camList,
      isMobileDevice,
    };
  },

  created() {
    window.addEventListener('resize', checkModile);
  },
  unmounted() {
    window.removeEventListener('resize', checkModile);
  },
});
</script>
