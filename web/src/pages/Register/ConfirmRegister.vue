<template>
  <q-page class="window-height window-width row justify-center items-center">
    <q-linear-progress query v-if="!isLoaded" />
    <div v-if="isLoaded" class="col-11 col-sm-8 col-md-6 col-lg-4">
      <q-card>
        <q-card-section v-if="HashError">
          Не корректная ссылка!
          <div><router-link to="/register">Зарегистрируйтесь</router-link></div>
          <div>
            Или проидите процедуру
            <router-link to="/recovery">восстановления пароля</router-link>
          </div>
        </q-card-section>
        <q-card-section v-if="!HashError">
          Регистрация завершена,
          <router-link to="/login">войдите в сервис</router-link>
        </q-card-section>
      </q-card>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router';
import { ref } from 'vue';
import { checkHash } from 'src/common/apiRequests';

name: 'ConfirmRegister';

const hash = useRoute().params.Hash as string;

const isLoaded = ref(false);
const HashError = ref(false);

checkHash(hash)
  .then(() => {
    isLoaded.value = true;
  })
  .catch(() => {
    isLoaded.value = true;
    HashError.value = true;
  });
// await utilites.CheckHash({ hash: this.Hash });
</script>
