<template>
  <video ref="video" class="video_container" muted="true" controls></video>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, watch } from 'vue';
import { conf } from 'src/boot/config';
import Hls from 'hls.js';
import Flv from 'flv.js';
import { storeToRefs } from 'pinia';
import { localUserStore } from 'src/stores/LocalUserStore';
import { dashBoardStore } from 'src/stores/DashBoardStore';

const props = defineProps({
  id: String,
  isFlv: Boolean,
  fullScreen: Boolean,
});

const userStore = localUserStore();
const dashStore = dashBoardStore();

const { mobileFullScreen } = storeToRefs(dashStore);

const video = ref(null);
const isMounted = ref(false);
const play_url = conf.hls_server + props.id + '/index.m3u8';
const flv_url =
  conf.hls_server + props.id + '.flv?authToken=' + userStore.getUserToken;
const hls = new Hls({
  debug: false,
  autoStartLoad: true,
  startPosition: 1,
});
const flv = Flv.createPlayer(
  {
    type: 'flv',
    url: flv_url,
    isLive: true,
  },
  { lazyLoadMaxDuration: 5 * 60, autoCleanupSourceBuffer: true },
);

// dashStore.$subscribe((state) => {
//   console.log(state);
// });

watch(
  mobileFullScreen,
  (first) => {
    if (first.device_uid == props.id) {
      if (first.state == true)
        (video.value! as HTMLMediaElement).requestFullscreen();
      else document.exitFullscreen();
    }
  },
  {
    immediate: true,
    deep: true,
  },
);

function makeVideo() {
  flv.unload();
  flv.detachMediaElement();
  hls.detachMedia();
  const _video = video.value as unknown as HTMLMediaElement;
  if (Flv.isSupported()) {
    if (props.isFlv) {
      flv.attachMediaElement(_video);
      flv.load();
      flv.play();
    } else {
      hls.loadSource(play_url);
      hls.attachMedia(_video);
      hls.on(Hls.Events.MEDIA_ATTACHED, function () {
        _video.play();
      });
    }
  } else {
    (_video as HTMLMediaElement).src = play_url;
  }
}

watch(
  props,
  function () {
    if (isMounted.value) makeVideo();
  },
  {
    immediate: true,
    deep: true,
  },
);

onMounted(() => {
  makeVideo();
  isMounted.value = true;
});

onBeforeUnmount(() => {
  flv.unload();
  flv.detachMediaElement();
  flv.destroy();
  hls.detachMedia();
  hls.destroy();
});
</script>

<style scoped>
.video_container {
  display: block;
  width: 100%;
  height: 100%;
}

video::-webkit-media-controls {
  display: none !important;
}
video::-webkit-media-controls-enclosure {
  display: none !important;
}
</style>
