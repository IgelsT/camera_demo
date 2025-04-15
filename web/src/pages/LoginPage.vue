<template>
  <q-page class="window-height window-width row justify-center items-center">
    <div class="col-11 col-sm-8 col-md-6 col-lg-4">
      <q-card class="elevation-12">
        <q-form ref="form" @submit="doLogin">
          <q-card-section class="bg-primary text-white" style="height: 55px">
            <div class="row justify-between">
              <div class="text-h6 col-auto">Вход</div>
              <div class="text-red text-bold text-right vertical-top col-sm-10" v-if="errorLogin">
                Ошибка: {{ errorText }}
              </div>
            </div>
          </q-card-section>
          <q-card-section v-if="!isLoaded" class="text-center">
            <q-circular-progress indeterminate size="50px" color="primary" />
          </q-card-section>
          <q-card-section v-if="isLoaded">
            <q-input v-model="UserName" label="Пользователь" dense :disable="el_disabled"
              :rules="[(v) => !!v || 'введите имя пользователя']" ref="FieldNumber">
              <template v-slot:prepend>
                <q-icon name="person" />
              </template>
            </q-input>
            <q-input v-model="Password" label="Пароль" type="password" :disable="el_disabled" dense
              :rules="[(v) => !!v || 'введите пароль']">
              <template v-slot:prepend>
                <q-icon name="lock" />
              </template>
            </q-input>
            <!-- <q-checkbox v-model="remember"
                    label="Запомнить"
                    :disable="el_disabled"
                    >
                    </q-checkbox> -->
          </q-card-section>
          <q-card-actions align="right" v-if="isLoaded">
            <q-circular-progress v-if="el_disabled" indeterminate size="50px" color="primary" />
            <div v-if="!el_disabled">
              <q-btn color="primary" type="submit">Вход</q-btn>
            </div>
          </q-card-actions>
        </q-form>
      </q-card>
      <div class="row q-mt-sm justify-between">
        <q-btn color="primary" to="/register" flat>Регистрация</q-btn>
        <q-btn color="primary" to="/recovery" flat>Восстановить пароль</q-btn>
        <q-btn color="primary" flat>
          <a href="/api/v1/apk/camera.apk">Скачать APK</a>
        </q-btn>
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">

name: 'LoginPage';

import { useRoute } from 'vue-router';
import { ref } from 'vue';
import { localUserStore } from '../stores/LocalUserStore';
import router from 'src/router';
import { hashString } from 'src/common/utils';

const isLoaded = true;

const el_disabled = ref(false);
const errorLogin = ref(false);
const errorText = ref();
const UserName = ref('');
const Password = ref('');

if (useRoute().query.message) {
  errorText.value = useRoute().query.message;
  errorLogin.value = true;
}

const doLogin = async () => {
  el_disabled.value = true;
  localUserStore()
    .login(UserName.value, hashString(Password.value))
    .then((resp) => {
      if (!resp) {
        errorLogin.value = true;
        errorText.value = 'проверьте логин/пароль';
      } else router.push({ path: '/' });
      el_disabled.value = false;
    })
    .catch((err) => {
      console.log(err);
      errorLogin.value = true;
      errorText.value = err.message;
      el_disabled.value = false;
    });
};
</script>

<style scoped>
.error-text {
  word-break: normal;
}
</style>
