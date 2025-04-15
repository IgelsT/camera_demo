<template>
  <q-linear-progress query v-if="!isLoaded" />

  <div v-if="isLoaded">
    <div v-if="store.mobileViewCols == 1" class="q-pt-sm">
      <div
        v-for="item in camList"
        :key="item as any"
        class="videoWrapper q-px-sm q-py-sm"
        @click="fullScreen(item.device_uid)"
      >
        <VideoComp :id="item.device_uid" :isFlv="isFlv" />
      </div>
    </div>
    <div v-if="store.mobileViewCols == 2">
      <div v-for="index in tableLines" :key="index" class="row q-py-sm">
        <div
          class="videoWrapper col q-px-sm q-py-sm"
          @click="fullScreen(store.deviceList[(index - 1) * 2].device_uid)"
        >
          <VideoComp
            v-if="checkIndex((index - 1) * 2)"
            :id="store.deviceList[(index - 1) * 2].device_uid"
            :isFlv="isFlv"
          />
        </div>
        <div
          class="videoWrapper col q-px-sm q-py-sm"
          @click="fullScreen(store.deviceList[(index - 1) * 2 + 1].device_uid)"
        >
          <VideoComp
            v-if="checkIndex((index - 1) * 2 + 1)"
            :id="store.deviceList[(index - 1) * 2 + 1].device_uid"
            :isFlv="isFlv"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { dashBoardStore, deviceParams } from 'src/stores/DashBoardStore';
import VideoComp from 'src/components/VideoComp.vue';

name: 'DashBoardMobile';

const store = dashBoardStore();
const camList = ref<Array<deviceParams>>();
const tableLines = computed(() => Math.ceil(store.deviceList.length / 2));
const isLoaded = ref(false);
const isFlv = ref(true);

onMounted(() => {
  store.getDashBoard().then(() => {
    isLoaded.value = true;
    camList.value = store.$state.deviceList;
  });
});

const checkIndex = (index: number) => {
  if (typeof store.deviceList[index] === 'undefined') return false;
  return true;
};

function fullScreen(device_uid: string) {
  store.setMobileFullScreen(device_uid);
}
</script>

<style scoped>
.videoWrapper {
  width: 100%;
  border: 1px solid black;
}

.video {
  width: 100%;
}
</style>
