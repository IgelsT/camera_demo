import { defineStore } from 'pinia';
import { apiLogin, apiSaveProfile } from 'src/common/apiRequests';
import router from '../../router';
import { conf } from '../../boot/config';
import { LocalStorage } from 'quasar';

type UserState = {
  isLogin: boolean;
  userToken: string;
  userName: string;
  userFull: LocalUser;
};

class LocalUser {
  user_description = '';
  user_id = 0;
  user_lastactivity = '';
  user_name = '';
}

const tokenKey = conf.LocalStorageString + '-token';
const userNameKey = conf.LocalStorageString + '-userName';
const isLoginKey = conf.LocalStorageString + '-islogin';
// async function delayMS(ms: number) {
//   return new Promise((resolve) => setTimeout(resolve, ms));
// }

function saveToLocalStore(state: UserState) {
  LocalStorage.set(isLoginKey, state.isLogin);
  LocalStorage.set(tokenKey, state.userToken);
  LocalStorage.set(userNameKey, state.userName);
}

function loadFromLocalStore(state: UserState) {
  state.isLogin = LocalStorage.getItem(isLoginKey) || false;
  state.userToken = LocalStorage.getItem(tokenKey) || '';
  state.userName = LocalStorage.getItem(userNameKey) || '';
}

export const localUserStore = defineStore('localUserStore', {
  state: () =>
    ({
      isLogin: false,
      userToken: '',
      userName: '',
      userFull: new LocalUser(),
    } as UserState),

  getters: {
    getUserLogin: (state) => {
      loadFromLocalStore(state);
      return state.isLogin;
    },
    getUserToken: (state) => state.userToken,
  },

  actions: {
    init() {
      console.log(this.$state);
    },

    async login(userName: string, userPass: string) {
      const result = await apiLogin(userName, userPass);
      Object.assign(this.userFull, result.user);
      this.userToken = result.hash;
      this.userName = this.userFull.user_name;
      this.isLogin = true;
      saveToLocalStore(this.$state);
      return true;
    },

    logout(message: string) {
      this.isLogin = false;
      saveToLocalStore(this.$state);
      let path = '/login';
      if (message) path += '?message=' + message;
      router.push({ path });
    },

    async saveProfile(pass: string, sendToDevice: boolean) {
      await apiSaveProfile({
        user_id: this.userName,
        user_password: pass,
        send_todevice: sendToDevice,
      });
    },
  },
});
