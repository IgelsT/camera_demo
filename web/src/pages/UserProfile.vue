<template>
  <q-page>
    <div class="text-h6">Профиль</div>
    <!-- <q-card style="max-width: 300px">
       <q-form ref="editVendorForm" @submit="SaveNumber" :disabled="el_disabled">
        <q-card-section class="row items-center q-pb-none"> </q-card-section>
        <q-card-section class="q-pt-none">
          <q-input v-model="MNumber" label="Мобильный" dense :rules="[val => !!val || 'Поле обязательно']" />
        </q-card-section>

        <q-card-actions align="right">
          <q-circular-progress v-if="el_disabled" indeterminate size="30px" color="primary" />
          <q-space />
          <q-btn flat label="Сохранить" color="primary" type="submit" />
        </q-card-actions>
      </q-form>
    </q-card> -->
    <q-card style="max-width: 600px">
      <q-form ref="editVendorForm" @submit="SavePassword">
        <q-card-section class="q-pt-none">
          <div class="row">
            <div class="col q-pr-sm">
              <q-input v-model="Password" dense type="password" label="Пароль"
                :rules="[(val: any) => !!val || 'Поле обязательно']" ref="fldPassword" />
            </div>
            <div class="col">
              <q-input v-model="PasswordRepeat" dense type="password" label="Повторите пароль" lazy-rules
                v-bind:rules="ConfirmPWD" ref="fldPasswordConfirm" />
            </div>
          </div>
          <!-- <div class="row">
            <q-checkbox
              v-model="sendToDevices"
              label="Разослать на устройства"
            />
          </div> -->
        </q-card-section>

        <q-card-actions align="right">
          <q-circular-progress v-if="el_disabled" indeterminate size="30px" color="primary" />
          <q-space />
          <q-btn flat label="Сохранить" color="primary" type="submit" />
        </q-card-actions>
      </q-form>
    </q-card>
  </q-page>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { localUserStore } from 'src/stores/LocalUserStore';
import {
  ConfirmDialog,
  ProcessDilog,
  showSnack,
} from 'src/common/popUpMessages';
import { hashString } from 'src/common/utils';

name: 'UserProfile';

const store = localUserStore();

const el_disabled = ref(false);
const Password = ref('');
const PasswordRepeat = ref('');
const sendToDevices = ref(true);

// export default {

const ConfirmPWD = computed(() => [
  (v: string) => !!v || 'Поле обязательно',
  (v: string) => v == Password.value || 'Пароли не совпадают',
]);

const SavePassword = () => {
  ConfirmDialog(
    'Изменить пароль!',
    'Вы уверены?',
    async () => {
      ProcessDilog(true, 'Сохранение');
      store.saveProfile(hashString(Password.value), sendToDevices.value).then(() => {
        ProcessDilog(false);
        showSnack(true, 'Сохранено');
      });
    },
  );
};
</script>
