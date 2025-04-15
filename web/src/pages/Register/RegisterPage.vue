<template>
  <q-page class="window-height window-width row justify-center items-center">
    <div class="col-11 col-sm-8 col-md-6 col-lg-4">
      <q-card class="elevation-12" v-if="!RegConfirm">
        <q-form ref="form" @submit="sendRegister">
          <q-card-section class="bg-primary text-white" style="height: 55px">
            <div class="text-h6 float-left">Регистрация</div>
          </q-card-section>
          <q-card-section>
            <q-input label="Email" v-model="Email" type="email" dense :disable="el_disabled"
              :rules="[(v: any) => !!v || 'Поле обязательно', isValidEmail]">
              <template v-slot:prepend>
                <q-icon name="email" />
              </template>
            </q-input>
            <q-input label="Пароль" type="password" v-model="Password" dense :disable="el_disabled" :rules="[
              (v: any) => !!v || 'введите пароль',
              (val: any) => val.length >= 6 || 'должно быть >6 символов',
            ]">
              <template v-slot:prepend>
                <q-icon name="lock" />
              </template>
            </q-input>
            <q-input label="Повторите пароль" type="password" v-model="Password2" dense :disable="el_disabled"
              :rules="[(v: any) => !!v || 'Поле обязательно', equalPass]">
              <template v-slot:prepend>
                <q-icon name="lock" />
              </template>
            </q-input>
          </q-card-section>
          <q-card-actions align="right">
            <div class="col q-pl-sm" v-if="ErrorRegister">
              <div class="text-red text-bold vertical-top">
                Ошибка: {{ ErrorText }}
              </div>
              <div>
                Или проидите процедуру
                <router-link to="/recovery">восстановления пароля</router-link>
              </div>
            </div>
            <q-circular-progress v-if="el_disabled" indeterminate size="50px" color="primary" />
            <div v-if="!el_disabled">
              <q-btn color="primary" type="submit">Отправить</q-btn>
            </div>
          </q-card-actions>
        </q-form>
      </q-card>
      <div class="row q-mt-sm justify-between" v-if="!el_disabled">
        <q-btn color="primary" to="/login" flat>Вход</q-btn>
        <q-btn color="primary" to="/recovery" flat>Восстановить пароль</q-btn>
      </div>
      <q-card v-if="RegConfirm">
        <q-card-section>
          Для завершения регистрации перейдите по ссылке, отправленной на
          {{ Email }}
        </q-card-section>
      </q-card>
    </div>
  </q-page>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import { registerStore } from '../../stores/RegisterStore';
import { hashString } from 'src/common/utils';


name: 'RegisterPage';

const Email = ref('');
const Password = ref('');
const Password2 = ref('');
const ErrorRegister = ref(false);
const ErrorText = ref('');
const el_disabled = ref(false);
const RegConfirm = ref(false);

const store = registerStore();

function isValidEmail() {
  const emailPattern =
    /^(?=[a-zA-Z0-9@._%+-]{6,254}$)[a-zA-Z0-9._%+-]{1,64}@(?:[a-zA-Z0-9-]{1,63}\.){1,8}[a-zA-Z]{2,63}$/;
  return emailPattern.test(Email.value) || 'Некорректный email';
}

function equalPass() {
  return Password.value == Password2.value || 'Пароли не совпадают';
}

function sendRegister() {
  ErrorRegister.value = false;
  el_disabled.value = true;
  store
    .sendRegister(Email.value, hashString(Password.value))
    .then(() => {
      RegConfirm.value = true;
    })
    .catch((err) => {
      el_disabled.value = false;
      if (err.code == 20) {
        ErrorRegister.value = true;
        ErrorText.value = err;
      }
    });
}
</script>
