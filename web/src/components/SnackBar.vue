<template>
  <q-dialog v-model="dialog" persistent>
    <q-card>
      <q-card-section class="row items-center q-pb-none"> </q-card-section>
      <q-card-section class="q-pt-none q-pb-none" style="height: 100%">
        <div class="row">
          <div class="col-auto q-mr-sm">
            <q-circular-progress indeterminate size="30px" color="primary" />
          </div>
          <div class="col-auto">
            <h6>{{ dialogTitle }}</h6>
          </div>
        </div>
        <div class="row full-width items-center q-mt-sm">
          {{ dialogMessage }}
        </div>
      </q-card-section>
      <q-card-actions align="right"> </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
import emiter from '../boot/appbus';
import { defineComponent, ref } from 'vue';
import { useQuasar } from 'quasar';
import { onBeforeUnmount } from 'vue';
import {
  IProgressData,
  ISnackData,
  IConfirmDialogData,
} from 'src/common/popUpMessages';

export default defineComponent({
  name: 'SnackBar',

  setup() {
    const $q = useQuasar();

    const dialog = ref(false);
    const dialogTitle = ref('');
    const dialogMessage = ref('');
    let progressTimer: NodeJS.Timeout | undefined = undefined;

    emiter.on('show_snack', (idata: unknown) => {
      ShowSnack(idata as ISnackData);
    });

    const ShowSnack = (data: ISnackData) => {
      if (data.success) {
        $q.notify({
          position: 'top',
          timeout: 1500,
          textColor: 'white',
          progress: true,
          multiLine: true,
          message: data.message,
          color: 'green',
        });
      } else {
        $q.notify({
          position: 'top',
          timeout: 5000,
          textColor: 'white',
          progress: true,
          multiLine: true,
          message: data.message,
          color: 'red',
          actions: [
            {
              label: 'Закрыть',
              color: 'white',
              handler: () => {
                /**/
              },
            },
          ],
        });
      }
    };

    emiter.on('show_confirm', (idata) => {
      const data = idata as IConfirmDialogData;
      $q.dialog({
        title: data.title,
        message: data.message,
        cancel: true,
        persistent: true,
        focus: 'cancel',
      }).onOk(() => {
        data.actionOK();
      });
    });

    emiter.on('show_process', (idata) => {
      const data = idata as IProgressData;
      dialog.value = data.state;
      if (data.state) {
        dialogTitle.value = data.title;
        dialogMessage.value = data.message;
        progressTimer = setTimeout(() => {
          if (dialog.value == true) {
            ShowSnack({ success: false, message: 'Таймаут процесса' });
            dialog.value = false;
          }
        }, 5000);
      } else {
        if (progressTimer) {
          clearInterval(progressTimer);
          progressTimer = undefined;
        }
      }
    });

    onBeforeUnmount(() => {
      emiter.off('*');
    });

    return { dialog, dialogTitle, dialogMessage };
  },
});
</script>
