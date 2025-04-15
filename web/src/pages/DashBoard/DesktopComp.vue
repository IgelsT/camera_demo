<template>
  <q-linear-progress query v-if="!isLoaded" />
  <div v-if="isLoaded">
    <!-- <q-toggle v-model="isFlv" label="HLS/FLV" /> -->
    <GridLayout
      v-model:layout="layout"
      :col-num="colNums"
      :row-height="rowHeight"
      :margin="margin"
      is-draggable
      is-resizable
      :vertical-compact="true"
      use-css-transforms
    >
      <GridItem
        v-for="(item, i) in layout"
        :static="item.static"
        :x="item.x"
        :y="item.y"
        :w="item.w"
        :h="item.h"
        :i="item.i"
        :key="item.i"
        :min-width="2"
        :min-height="2"
        @resized="resizedEvent"
        @moved="movedEvent"
      >
        <!--
        @resize="resizeEvent"
        @move="moveEvent"
                @container-resized="containerResizedEvent"
       -->
        <span class="videoWrapper">
          <div class="row justify-end">
            <div class="col q-pt-sm q-pl-sm">{{ item.name }}</div>
            <div clas="col-auto">
              <q-btn
                dense
                round
                flat
                color="black"
                icon="fullscreen"
                @click="fullScreen(i)"
              >
                <q-tooltip :delay="500">Развернуть</q-tooltip>
              </q-btn>
              <router-link
                :to="{
                  name: 'DeviceDetail',
                  params: { DeviceId: item.id },
                }"
                class="table-button"
              >
                <q-btn dense round flat color="black" icon="visibility">
                  <q-tooltip :delay="500">Детали</q-tooltip>
                </q-btn>
              </router-link>
            </div>
            <q-btn
              dense
              round
              flat
              color="black"
              icon="close"
              @click="DeleteDevice(item.id)"
            >
              <q-tooltip :delay="500">Убрать из сводки</q-tooltip>
            </q-btn>
          </div>
          <div
            style="height: calc(100% - 45px)"
            :ref="
              (el) => {
                videoHolders[i] = el as Element;
              }
            "
          >
            <div
              :ref="
                (el) => {
                  videoFrames[i] = el as Element;
                }
              "
              style="height: 100%"
            >
              <VideoComp :id="item.i" :isFlv="isFlv" />
            </div>
          </div>
        </span>
      </GridItem>
    </GridLayout>
    <q-dialog v-model="dialogFullscreen" persistent @show="showDialog">
      <q-card class="fullScreenDiv">
        <div class="row justify-end">
          <div class="col q-pt-sm q-pl-sm">{{}}</div>
          <div>
            <q-btn
              dense
              round
              flat
              color="black"
              icon="close"
              @click="closeDialog()"
            >
              <q-tooltip :delay="500">Закрыть</q-tooltip>
            </q-btn>
          </div>
        </div>
        <div ref="tomove" style="height: calc(100% - 45px); width: 100%"></div>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';
import VideoComp from 'src/components/VideoComp.vue';
import Hls from 'hls.js';
import Flv from 'flv.js';
import {
  dashBoardStore,
  deviceParams,
  gridItemType,
} from 'src/stores/DashBoardStore';
import { ConfirmDialog, ProcessDilog } from 'src/common/popUpMessages';
import { GridLayout, GridItem } from 'vue3-grid-layout-next';
name: 'DashBoardDesktop';
// const props = defineProps({
//   camList: Array,
// });

type videoElem = {
  id: string;
  hls?: Hls;
  flv?: Flv.Player;
  el: HTMLMediaElement;
};

const store = dashBoardStore();
const isLoaded = ref(false);

const videoElArray: Array<videoElem> = [];

const rowHeightConst = 30;
const colNumsConstant = 12;
const marginConstant = 10;

const colNums = ref(colNumsConstant);
const rowHeight = ref(rowHeightConst);
const margin = ref([marginConstant, marginConstant]);
let layout = ref<Array<gridItemType>>([]);
const isFlv = ref(true);
const videoHolders = ref<Array<Element>>([]);
const videoFrames = ref<Array<Element>>([]);
const tomove = ref(null);
const dialogFullscreen = ref(false);
const currentEl = ref<number>(-1);

function makeLayout() {
  const savedLayout = store.getDesktopLayout();
  // const layout: Array<gridItemType> = [];
  for (const row of store.deviceList || []) {
    let rowIndex = 0;
    const frame: gridItemType = {
      x: 0,
      y: rowIndex,
      w: 6,
      h: 6,
      i: (row as deviceParams).device_uid,
      static: false,
      id: (row as deviceParams).device_id,
      name: (row as deviceParams).device_name,
    };
    const idx = savedLayout.findIndex(
      (el) => el.i == (row as deviceParams).device_uid,
    );
    if (idx >= 0) {
      frame.x = savedLayout[idx].x;
      frame.y = savedLayout[idx].y;
      frame.w = savedLayout[idx].w;
      frame.h = savedLayout[idx].h;
    }
    layout.value.push(frame);
    rowIndex += 6;
  }
  return layout;
}

onMounted(() => {
  store.getDashBoard().then(() => {
    isLoaded.value = true;
    makeLayout();
  });
});

onBeforeUnmount(() => {
  for (const el of videoElArray) {
    if (isFlv.value) {
      el.flv?.unload();
    }
  }
});

// function moveEvent(i: string, newX: number, newY: number) {
//   console.log('MOVE i=' + i + ', X=' + newX + ', Y=' + newY);
// }

// function resizeEvent(i: string,newH: number, newW: number, newHPx: number, newWPx: number) {
//   console.log( 'RESIZE i=' +      i +      ', H=' +      newH +      ', W=' +      newW +
//       ', H(px)=' +      newHPx +      ', W(px)=' +      newWPx
//   );
// }

// function containerResizedEvent(i: string, newH: number,newW: number,newHPx: number,newWPx: number) {
//   console.log('CONTAINER RESIZED i=' +  i +     ', H=' +     newH +     ', W=' +     newW +
//       ', H(px)=' +     newHPx +     ', W(px)=' +     newWPx );
// }

function movedEvent() {
  // i: string, newX: number, newY: number
  // console.log('MOVED i=' + i + ', X=' + newX + ', Y=' + newY);
  store.saveDesktopLayout(layout.value);
}

function resizedEvent() {
  //   i: string,  newH: number,  newW: number,  newHPx: number,  newWPx: number
  // console.log('RESIZED i=' + i + ', H=' + newH + ', W=' + newW + ', H(px)=' + newHPx + ', W(px)=' + newWPx  );
  store.saveDesktopLayout(layout.value);
}

function DeleteDevice(id: number) {
  ConfirmDialog('Удалить', 'Удалить камеру из сводки?', () => {
    ProcessDilog(true, 'Удаление...');
    store.setCamToDash(id, false).then(() => {
      layout.value = [];
      makeLayout();
      ProcessDilog(false);
    });
  });
}

function fullScreen(id: number) {
  // console.log(id);
  // console.log(videoFrames);
  currentEl.value = id;
  dialogFullscreen.value = true;
}

function showDialog() {
  const el = videoFrames.value[currentEl.value];
  const holder = videoHolders.value[currentEl.value];
  if (tomove.value != null) {
    holder.removeChild(el);
    // console.log(tomove.value);
    (tomove.value as Element).appendChild(el);
  }
}

function closeDialog() {
  const el = videoFrames.value[currentEl.value];
  const holder = videoHolders.value[currentEl.value];
  if (tomove.value != null) {
    (tomove.value as Element).removeChild(el);
    holder.appendChild(el);
  }
  dialogFullscreen.value = false;
}
</script>

<style scoped>
/* .video {
  border: 1px solid black;
  width: 100%;
  height: 80%;
  padding-bottom: 15px;
} */

.fullScreenDiv {
  width: 100%;
  height: 100%;
  max-width: 100%;
}
.vue-grid-layout {
  background: #eee;
}
.vue-grid-item:not(.vue-grid-placeholder) {
  background: #ccc;
  border: 1px solid black;
}
.vue-grid-item .resizing {
  opacity: 0.9;
}
.vue-grid-item .static {
  background: #cce;
}
.vue-grid-item .text {
  font-size: 24px;
  text-align: center;
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  margin: auto;
  height: 100%;
  width: 100%;
}
.vue-grid-item .no-drag {
  height: 80%;
  width: 100%;
}
.vue-grid-item .minMax {
  font-size: 12px;
}
.vue-grid-item .add {
  cursor: pointer;
}
.vue-draggable-handle {
  position: absolute;
  width: 20px;
  height: 20px;
  top: 0;
  left: 0;
  background: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='10' height='10'><circle cx='5' cy='5' r='5' fill='#999999'/></svg>")
    no-repeat;
  background-position: bottom right;
  padding: 0 8px 8px 0;
  background-repeat: no-repeat;
  background-origin: content-box;
  box-sizing: border-box;
  cursor: pointer;
}
</style>
