<template>
  <q-page>
    <q-linear-progress query v-if="!isLoaded" />
    <div v-if="isLoaded">
      <div class="row q-mb-sm justify-end">
        <div v-if="deviceMessages.length > 0">
          <div class="row">
            <div class="col-auto vertical-middle" style="display: inline-table">
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
        <div v-else>
          <q-btn
            color="primary"
            dense
            v-if="!isWaitForAnswer"
            @click="updateLog"
          >
            Request log
          </q-btn>
        </div>
      </div>
      <q-table
        :rows="deviceLogList"
        :columns="columns as any"
        class="stvs-table"
        style="height: 100%"
        virtual-scroll
        dense
        row-key="log_id"
        :rows-per-page-options="[40, 80, 0]"
      >
        <template v-slot:body-cell-actions="props">
          <q-td :props="props">
            <q-btn
              dense
              round
              flat
              color="grey"
              icon="download"
              @click="getFile(props.row.log_name)"
              v-if="props.row.file"
            >
              <q-tooltip :delay="500">Download file</q-tooltip>
            </q-btn>
            <q-btn
              dense
              round
              flat
              color="grey"
              icon="smartphone"
              @click="requestLogFile(props.row.log_name)"
            >
              <q-tooltip :delay="500">Request file</q-tooltip>
            </q-btn>
          </q-td>
        </template>
      </q-table>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router';
import { deviceStore } from '../../stores/DeviceStore';
import { ref, onMounted } from 'vue';
import { storeToRefs } from 'pinia';
import { ConfirmDialog, ProcessDilog } from 'src/common/popUpMessages';

name: 'DeviceLogs';

const deviceID = parseInt(useRoute().params.DeviceId as string);
const store = deviceStore();

const isLoaded = ref(false);
const { deviceLogList, deviceMessages } = storeToRefs(store);

const isWaitForAnswer = ref(false);

const columns = [
  {
    name: 'name',
    label: 'file',
    field: 'log_name',
    align: 'left',
    sortable: true,
  },
  { name: 'actions', label: '', field: '', align: 'right' },
];

onMounted(() => {
  getData();
});

function getData() {
  isLoaded.value = false;
  store.getDeviceInfo(deviceID).then(() => {
    isLoaded.value = true;
    if (deviceMessages.value.length > 0) startRefresh();
  });
  isLoaded.value = false;
  store.getDeviceLogs(deviceID).then(() => {
    isLoaded.value = true;
  });
}

function startRefresh() {
  setTimeout(getData, 10000);
}

function deleteMessages() {
  ConfirmDialog('Удалить очередь сообщений?', '', () => {
    ProcessDilog(true, 'Удалние');
    store.delDeviceMessages(deviceID).then(() => {
      ProcessDilog(false);
    });
  });
}

function getFile(name: string) {
  console.log(name);
  // store.getDeviceLogFile(deviceID, name).then((response) => {
  //   const filename = response.headers['content-disposition']
  //     .split('=')[1]
  //     .replaceAll('"', '');
  //   var blob = new Blob([response.data], {
  //     type: 'application/zip',
  //   });
  //   var a = document.createElement('a');
  //   document.body.appendChild(a);
  //   const url = window.URL.createObjectURL(blob);
  //   a.href = url;
  //   a.download = filename;
  //   a.click();
  //   window.URL.revokeObjectURL(url);
  //   a.remove();
  // });
}

function requestLogFile(name: string) {
  store.requestLogFile(deviceID, name).then(() => {
    startRefresh();
  });
}

function updateLog() {
  store.updateDeviceLog(deviceID).then(() => {
    startRefresh();
  });
}
</script>
