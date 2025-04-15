<template>
  <q-dialog v-model="dialog" persistent>
    <q-card class="editDialog">
      <q-form ref="form" @submit="saveLocal" class="full-width">
        <q-card-section class="row full-width">
          <q-input
            v-model="deviceInfo.device_name"
            dense
            label="Имя"
            class="full-width"
            :rules="[(val: any) => !!val || 'Поле обязательно']"
          />
          <q-input
            class="full-width"
            v-model="deviceInfo.device_description"
            type="textarea"
            autogrow
            label="Описание"
            dense
            :rules="[
              (val: any) => !!val || 'Поле обязательно',
              (val: any) => val.length >= 10 || 'должно быть >10 символов',
            ]"
          />
          <q-select
            v-model="deviceInfo.device_access"
            input-debounce="0"
            label="Доступ"
            :options="ddlValues.Access"
            option-value="id"
            option-label="value"
            map-options
            emit-value
            dense
            class="full-width"
          />
          <q-select
            v-model="deviceInfo.device_camera_id"
            input-debounce="0"
            label="Камера"
            :options="store.$state.deviceCams"
            option-value="camera_num"
            option-label="camera_type"
            map-options
            emit-value
            dense
            class="full-width"
          />
          <q-select
            v-model="deviceInfo.device_resolution"
            input-debounce="0"
            label="Разрешение"
            :options="cameraResolution"
            dense
            class="full-width"
          />
          <q-select
            v-model="deviceInfo.device_focus"
            input-debounce="0"
            label="Фокус"
            :options="cameraFocus"
            dense
            class="full-width"
          />
          <q-select
            v-model="deviceInfo.device_fps"
            input-debounce="0"
            label="FPS"
            :options="ddlValues.FPS"
            dense
            class="full-width"
          />
          <q-select
            v-model="deviceInfo.device_quality"
            input-debounce="0"
            label="Качество"
            :options="ddlValues.Quality"
            option-value="id"
            option-label="value"
            map-options
            emit-value
            dense
            class="full-width"
          />
          <q-select
            v-model="deviceInfo.device_orientation"
            input-debounce="0"
            label="Ориентация"
            :options="ddlValues.Orientation"
            option-value="id"
            option-label="value"
            map-options
            emit-value
            dense
            class="full-width"
          />
          <q-select
            v-model="deviceInfo.device_status"
            input-debounce="0"
            label="Трансляция"
            :options="ddlValues.Stream"
            option-value="id"
            option-label="value"
            map-options
            emit-value
            dense
            class="full-width"
          />
          <q-select
            v-model="deviceInfo.on_dash"
            label="В сводке"
            :options="ddlValues.Ondash"
            option-value="id"
            option-label="value"
            map-options
            emit-value
            dense
            class="full-width"
          />
        </q-card-section>

        <q-card-actions align="right">
          <q-btn flat label="Отмена" color="primary" @click="cancelSave()" />
          <q-btn flat label="Сохранить" color="primary" type="submit" />
        </q-card-actions>
      </q-form>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
import { computed, ComputedRef, defineComponent, ref } from 'vue';
import { deviceStore, ddlValues } from '../../stores/DeviceStore';

const store = deviceStore();
const deviceInfo = ref(store.deviceToApi());

const cameraResolution: ComputedRef<string[]> = computed((): string[] => {
  const allResolutions =
    store.$state.deviceCams
      .find((el) => el.camera_num == deviceInfo.value.device_camera_id)
      ?.camera_resolutions.split(',') || [];
  if (!allResolutions.includes(deviceInfo.value?.device_resolution || ''))
    deviceInfo.value.device_resolution = allResolutions[0];
  return allResolutions;
});

export default defineComponent({
  name: 'DeviceEditCom',

  props: {
    save: Function,
    cancel: Function,
  },

  computed: {
    dialog(): boolean {
      return true;
    },

    cameraFocus(): string[] {
      const allFocuses =
        store.$state.deviceCams
          .find((el) => el.camera_num == deviceInfo.value.device_camera_id)
          ?.camera_focuses.split(',') || [];
      if (!allFocuses.includes(deviceInfo.value?.device_focus || ''))
        deviceInfo.value.device_focus = allFocuses[0];
      return allFocuses;
    },
  },

  setup(props) {
    deviceInfo.value = store.deviceToApi();
    const saveLocal = () => {
      if (props.save) props.save(deviceInfo.value);
    };

    const cancelSave = () => {
      if (props.cancel) props.cancel();
    };

    return {
      deviceInfo,
      ddlValues,
      cameraResolution,
      saveLocal,
      cancelSave,
      store,
    };
  },
});
</script>
