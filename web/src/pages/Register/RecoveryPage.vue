<template>
  <q-page class="window-height window-width row justify-center items-center">
    <div class="col-11 col-sm-8 col-md-6 col-lg-4">
      <q-card class="elevation-12" v-if="!RegConfirm">
        <q-form ref="form" @submit="Recovery">
          <q-card-section class="bg-primary text-white" style="height: 55px">
            <div class="text-h6 float-left">Восстановить пароль</div>
          </q-card-section>
          <q-card-section>
            <q-input
              label="Email"
              v-model="Email"
              type="email"
              dense
              :disable="el_disabled"
              :rules="[(v:any) => !!v || 'Поле обязательно', isValidEmail]"
            >
              <template v-slot:prepend>
                <q-icon name="email" />
              </template>
            </q-input>
          </q-card-section>
          <q-card-actions align="right">
            <div class="col q-pl-sm" v-if="ErrorRecovery">
              <div class="text-red text-bold vertical-top">
                Ошибка: {{ ErrorText }}
              </div>
              <div>
                <router-link to="/register">Зарегистрируйтесь</router-link>
              </div>
            </div>
            <q-circular-progress
              v-if="el_disabled"
              indeterminate
              size="50px"
              color="primary"
            />
            <div v-if="!el_disabled">
              <q-btn color="primary" type="submit">Отправить</q-btn>
            </div>
          </q-card-actions>
        </q-form>
      </q-card>
      <q-card v-if="RegConfirm">
        <q-card-section>
          Новый пароль был отправлен на {{ Email }}
          <router-link to="/login">войдите в сервис</router-link>
        </q-card-section>
      </q-card>
      <div class="row q-mt-sm justify-between" v-if="!el_disabled">
        <q-btn color="primary" to="/login" flat>Вход</q-btn>
        <q-btn color="primary" to="/register" flat>Регистрация</q-btn>
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { registerStore } from '../../stores/RegisterStore';

name: 'RecoveryPage';

const store = registerStore();

const Email = ref('');
const el_disabled = ref(false);
const ErrorRecovery = ref(false);
const RegConfirm = ref(false);
const ErrorText = ref('');

function isValidEmail() {
  const emailPattern =
    /^(?=[a-zA-Z0-9@._%+-]{6,254}$)[a-zA-Z0-9._%+-]{1,64}@(?:[a-zA-Z0-9-]{1,63}\.){1,8}[a-zA-Z]{2,63}$/;
  return emailPattern.test(Email.value) || 'Некорректный email';
}

function Recovery() {
  el_disabled.value = true;
  ErrorRecovery.value = false;
  store
    .sendRecovery(Email.value)
    .then(() => {
      RegConfirm.value = true;
    })
    .catch((error) => {
      el_disabled.value = false;
      console.log(error);
      if (error.code == 'EMAIL_NOT_EXIST') {
        ErrorRecovery.value = true;
        ErrorText.value = error.message;
      }
    });
  // utilites
  //   .Recovery({ user_email: this.Email })
  //   .then((response) => {
  //     //console.log(response);
  //     this.RegConfirm = true;
  //     this.el_disabled = false;
  //   })
  //   .catch((error) => {
  //     console.log(error);
  //     this.el_disabled = false;
  //     if (error.data.reason) {
  //       this.ErrorRecovery = true;
  //       if (error.data.reason == 'email_not_exist') {
  //         this.ErrorText = 'email не существует!';
  //       } else {
  //         this.ErrorText = error.data.reason;
  //       }
  //     }
  //   });
}
</script>
