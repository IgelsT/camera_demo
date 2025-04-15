<template>
  <q-page>
    <q-linear-progress query v-if="!isLoaded" />
    <div v-if="isLoaded">
      <div class="row q-mb-sm">
        <h6>Устройство: {{ deviceInfo?.device_name }}</h6>
      </div>
      <q-card>
        <q-card-actions>
          <div class="row full-width">
            <div class="col-auto devinfoTitle">Имя:</div>
            <div class="col">{{ deviceInfo.device_name }}</div>
          </div>
          <div class="row full-width">
            <div class="col-auto devinfoTitle">Описание:</div>
            <div class="col">{{ deviceInfo.device_description }}</div>
          </div>
          <div class="row full-width">
            <div class="col-auto devinfoTitle">Доступ:</div>
            <div class="col">
              {{
                ddlValues.Access.find((el) => el.id == deviceInfo.device_access)
                  ?.value
              }}
            </div>
          </div>

          <div class="row full-width">
            <div class="col-auto devinfoTitle">Питание:</div>
            <div class="col">
              {{
                (deviceInfo.device_power || 0) > 0 ? 'подключено' : 'отключено'
              }}
              заряд {{ Math.abs(deviceInfo?.device_power) }}%
            </div>
          </div>
          <div class="row full-width">
            <div class="col-auto devinfoTitle">Камера:</div>
            <div class="col">
              {{ cameraName }}
            </div>
          </div>

          <div class="row full-width">
            <div class="col-auto devinfoTitle">Разрешение:</div>
            <div class="col">
              {{ deviceInfo.device_resolution }}
            </div>
          </div>

          <div class="row full-width">
            <div class="col-auto devinfoTitle">Фокус:</div>
            <div class="col">
              {{ deviceInfo.device_focus }}
            </div>
          </div>

          <div class="row full-width">
            <div class="col-auto devinfoTitle">FPS:</div>
            <div class="col">
              {{ deviceInfo.device_fps }}
            </div>
          </div>

          <div class="row full-width">
            <div class="col-auto devinfoTitle">Качество:</div>
            <div class="col">
              {{
                ddlValues.Quality.find(
                  (el) => el.id == deviceInfo.device_quality,
                )?.value
              }}
            </div>
          </div>

          <div class="row full-width">
            <div class="col-auto devinfoTitle">Ориентация:</div>
            <div class="col">
              {{
                ddlValues.Orientation.find(
                  (el) => el.id == deviceInfo.device_orientation,
                )?.value
              }}
              (* какая сторона вверху)
            </div>
          </div>

          <div class="row full-width">
            <div class="col-auto devinfoTitle">Трансляция:</div>
            <div class="col">
              {{
                ddlValues.Stream.find((el) => el.id == deviceInfo.device_status)
                  ?.value
              }}
            </div>
          </div>
          <div class="row full-width">
            <div class="col-auto devinfoTitle">В сводке:</div>
            <div class="col">
              {{
                ddlValues.Ondash.find((el) => el.id == deviceInfo.on_dash)
                  ?.value
              }}
            </div>
          </div>
        </q-card-actions>
        <q-card-actions>
          <div class="row full-width justify-end" v-if="isWaitForAnswer">
            <q-circular-progress
              show-value
              class="text-light-blue q-ma-md"
              :value="waitProgress"
              size="50px"
              color="light-blue"
            >
              {{ waitValue }}
            </q-circular-progress>
          </div>
          <div v-if="deviceMessages.length > 0">
            <div class="row">
              <div
                class="col-auto vertical-middle"
                style="display: inline-table"
              >
                <span style="display: table-cell; vertical-align: middle">
                  Отправка изменений на устройство...
                </span>
              </div>
              <div class="col-auto">
                <q-btn
                  dense
                  round
                  flat
                  color="grey"
                  icon="delete"
                  @click="deleteMessages()"
                >
                  <q-tooltip :delay="500">Удалить</q-tooltip>
                </q-btn>
              </div>
            </div>
            <div class="row">
              <q-linear-progress query />
            </div>
          </div>
          <div class="row full-width justify-end" v-else>
            <q-btn
              color="primary"
              size="sm"
              label="Изменить"
              @click="openEdit()"
            ></q-btn>
          </div>
        </q-card-actions>
      </q-card>
      <DeviceEditCom
        v-if="dialog"
        :details="deviceInfo"
        :save="save"
        :cancel="() => (dialog = false)"
      ></DeviceEditCom>
      <!-- <q-card class="q-mt-sm">
        <q-expansion-item expand-separator label="Подробнее об устройстве">
          <q-card-actions>
            <div class="row full-width">
              <div
                v-for="cam in store.$state.deviceCams"
                :key="cam.camera_id"
                class="col-auto q-ml-sm"
              >
                <div>Камера {{ cam.camera_num }} тип {{ cam.camera_type }}</div>
                <li v-for="res in cam.camera_resolutions.split(',')" :key="res">
                  {{ res }}
                </li>
                <li v-for="focus in cam.camera_focuses.split(',')" :key="focus">
                  {{ focus }}
                </li>
              </div>
            </div>
          </q-card-actions>
          <q-card-actions>
            <div
              v-for="(value, name, index) in JSON.parse(
                deviceInfo?.device_info || ''
              )"
              :key="index"
              class="row full-width"
            >
              <div class="col-auto devInfoHeader">{{ name }}:</div>
              <div>{{ value }}</div>
            </div>
          </q-card-actions>
        </q-expansion-item>
      </q-card> -->
      <div class="q-pt-sm" v-if="deviceInfo.device_status == 1">
        <VideoComp :id="deviceInfo?.device_uid" :isFlv="isFlv" />
      </div>
    </div>
  </q-page>
</template>

<style>
.devinfoTitle {
  min-width: 90px !important;
  margin-right: 10px;
}

.devInfoHeader {
  min-width: 120px !important;
}

.editDialog {
  min-width: 300px !important;
}
</style>

<script lang="ts" setup>
name: 'DeviceDetail';

import { ref, computed, ComputedRef, onMounted } from 'vue';
import { deviceStore, ddlValues, DeviceToApi } from '../../stores/DeviceStore';
import DeviceEditCom from './DeviceEditCom.vue';
import VideoComp from 'src/components/VideoComp.vue';
// import { isMobile } from 'src/common/utils';
import { useRoute } from 'vue-router';
import { storeToRefs } from 'pinia';
import { ConfirmDialog, ProcessDilog } from 'src/common/popUpMessages';

// const ModuleHeight: ComputedRef<string> = computed((): string =>
//   isMobile.any() ? '' : 'height: calc(100vh - 60px);',
// );

const store = deviceStore();

const { deviceInfo, deviceMessages } = storeToRefs(store);
// const deviceInfo = store.deviceInfo;
// const deviceMsg = computed(() => store.deviceMessages);

// const countDownDef = 10;
// var countDown = countDownDef;

const isLoaded = ref(false);
const dialog = ref(false);
const isWaitForAnswer = ref(false);
const waitProgress = ref(0);
const waitValue = ref(0);
const isFlv = ref(true);
const deviceID = parseInt(useRoute().params.DeviceId as string);

function openEdit() {
  dialog.value = true;
}

function save(deviceInfo: DeviceToApi) {
  deviceStore()
    .saveDeviceParams(deviceInfo)
    .then(() => {
      dialog.value = false;
      if (deviceMessages.value.length > 0) startRefresh();
    });

  // waitValue.value = countDown = countDownDef;
  // waitProgress.value = 100;
  // setTimeout(countDownStep, 1000);
}

function startRefresh() {
  setTimeout(getData, 10000);
}

// function countDownStep() {
//   countDown--;
//   waitProgress.value = (100 / countDownDef) * countDown;
//   waitValue.value = countDown;
//   if (countDown > 0) {
//     setTimeout(countDownStep, 1000);
//   } else {
//     isWaitForAnswer.value = false;
//     getData();
//   }
// }

function getData() {
  if (deviceID != -1)
    store.getDeviceInfo(deviceID).then(() => {
      isLoaded.value = true;
      if (deviceMessages.value.length > 0) startRefresh();
    });
}

function deleteMessages() {
  ConfirmDialog('Удалить очередь сообщений?', '', () => {
    ProcessDilog(true, 'Удалние');
    store.delDeviceMessages(deviceID).then(() => {
      ProcessDilog(false);
    });
  });
}

const cameraName: ComputedRef<string> = computed(
  (): string =>
    store.$state.deviceCams.find(
      (el) => el.camera_num == deviceInfo.value.device_camera_id,
    )?.camera_type || '',
);

onMounted(() => {
  getData();
});
</script>
