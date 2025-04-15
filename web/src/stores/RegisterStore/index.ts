import { defineStore } from 'pinia';
import { recovery, register } from 'src/common/apiRequests';

export const registerStore = defineStore('registerStore', {
  state: () => ({}),

  getters: {},

  actions: {
    async sendRegister(email: string, password: string) {
      return register(email, password);
    },

    async sendRecovery(email: string) {
      return recovery(email);
    },
  },
});
