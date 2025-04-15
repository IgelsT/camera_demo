<template>
  <q-page style="width: 100%" ref="busstop">
    <q-linear-progress query v-if="!isLoaded" />
    <div v-if="isLoaded">
      <div class="row q-pb-sm" style="max-width: 100%">
        <div class="col">
          <q-input
            dense
            debounce="300"
            v-model="deviceFilter"
            placeholder="Поиск"
            clearable
            clear-icon="close"
            style="width: 100%"
          >
            <template v-slot:append>
              <q-icon name="search" />
            </template>
          </q-input>
        </div>
        <!-- <div class="col text-right">
          <q-btn color="primary" label="Новый" size="sm" :to="{ name: 'PatientDetail', params: { PatientId: 0 } }"></q-btn>
        </div> -->
      </div>

      <div class="row" :style="ModuleHeight" style="width: 100%">
        <q-table
          :rows="camList"
          :columns="columns as any"
          :filter="deviceFilter"
          class="stvs-table"
          style="height: 100%"
          virtual-scroll
          dense
          row-key="device_id"
          :rows-per-page-options="[40, 80, 0]"
        >
          <template v-slot:body-cell-device_online="props">
            <q-td :props="props">
              <div
                class="online-circle"
                :class="
                  props.row.device_online == 0
                    ? 'online-circle-red'
                    : 'online-circle-green'
                "
              ></div>
            </q-td>
          </template>
          <template v-slot:body-cell-actions="props">
            <q-td :props="props">
              <router-link
                :to="{
                  name: 'DeviceDetail',
                  params: { DeviceId: props.row.device_id },
                }"
                class="table-button"
              >
                <q-btn dense round flat color="grey" icon="visibility">
                  <q-tooltip :delay="500">Детали</q-tooltip>
                </q-btn>
              </router-link>
              <q-btn
                dense
                round
                flat
                color="grey"
                icon="delete"
                @click="DeleteDevice(props.row.device_id)"
              >
                <q-tooltip :delay="500">Удалить</q-tooltip>
              </q-btn>
            </q-td>
          </template>
        </q-table>
      </div>
    </div>
  </q-page>
</template>

<script lang="ts">
import { useQuasar } from 'quasar';
import { computed, ComputedRef, defineComponent, ref } from 'vue';
import { deviceStore, DeviceFromApi } from '../stores/DeviceStore';

const columns = [
  {
    name: 'device_online',
    label: '',
    field: 'device_online',
    align: 'left',
    style: 'width: 30px',
    sortable: false,
  },
  {
    name: 'device_name',
    label: 'Имя',
    field: 'device_name',
    align: 'left',
    sortable: true,
  },
  // {
  //   name: 'device_uid',
  //   label: 'Device UID',
  //   field: 'device_uid',
  //   align: 'left',
  //   sortable: false,
  // },
  {
    name: 'device_power',
    label: 'Заряд',
    field: 'device_power',
    align: 'left',
    sortable: false,
  },
  {
    name: 'device_status',
    label: 'Видео',
    field: (row: DeviceFromApi) => (row.device_status == 0 ? 'Off' : 'On'),
    align: 'left',
    sortable: false,
  },
  { name: 'actions', label: '', field: '', align: 'right' },
];

const ModuleHeight: ComputedRef<string> = computed(
  (): string => 'height: calc(100vh - 120px);'
);

export default defineComponent({
  name: 'DeviceList',

  setup() {
    const isLoaded = ref(false);
    const camList = ref<Array<DeviceFromApi>>([]);
    const deviceFilter = ref('');
    const $q = useQuasar();

    deviceStore()
      .getDeviceList()
      .then((resp) => {
        camList.value = resp;
        // console.log(resp);
        isLoaded.value = true;
      });

    const DeleteDevice = (id: number) => {
      const idx = camList.value?.findIndex((el) => el.device_id == id);
      const name = camList.value[idx].device_name;
      $q.dialog({
        title: `Удалить устройство ${name}?`,
        message:
          'Устройство будет больше недоступно! Необходимо будет снова пройти авторизацию на устройстве!',
        cancel: true,
        persistent: true,
      })
        .onOk(() => {
          deviceStore()
            .deleteDevice(id)
            .then(() => camList.value.splice(idx, 1));
        })
        .onCancel(() => {
          // console.log('>>>> Cancel')
        })
        .onDismiss(() => {
          // console.log('I am triggered on both OK and Cancel')
        });
    };

    return {
      isLoaded,
      camList,
      columns,
      ModuleHeight,
      deviceFilter,
      DeleteDevice,
    };
  },
});
</script>
