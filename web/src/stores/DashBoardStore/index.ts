import { defineStore } from 'pinia';
import { getDashBoard, setDeviceToDashBoard } from 'src/common/apiRequests';
import { conf } from '../../boot/config';
import { LocalStorage } from 'quasar';

type deviceParams = {
  device_fps: number;
  device_id: number;
  device_orientation: number;
  device_power: number;
  device_quality: number;
  device_resolution: string;
  device_uid: string;
  device_name: string;
};

type gridItemType = {
  i: string;
  x: number;
  y: number;
  w: number;
  h: number;
  static: boolean;
  id: number;
  name: string;
};

type devToFsType = {
  device_uid: string;
  state: boolean;
};

const desktopLayoutKey = conf.LocalStorageString + '-DesktopLayout';
const mobileLayoutKey = conf.LocalStorageString + '-DesktopLayout';

// function saveToLocalStore(state: UserState) {
//   LocalStorage.set(isLoginKey, state.isLogin);
//   LocalStorage.set(tokenKey, state.userToken);
//   LocalStorage.set(userNameKey, state.userName);
// }

// function loadFromLocalStore(state: UserState) {
//   state.isLogin = LocalStorage.getItem(isLoginKey) || false;
//   state.userToken = LocalStorage.getItem(tokenKey) || '';
//   state.userName = LocalStorage.getItem(userNameKey) || '';
// }

export const dashBoardStore = defineStore('dashBoardStore', {
  state: () => ({
    deviceList: [] as Array<deviceParams>,
    isMobileDevice: false,
    mobileViewCols: 1,
    mobileFullScreen: {} as devToFsType,
  }),

  getters: {
    // getUserLogin: (state) => {
    //   loadFromLocalStore(state);
    //   return state.isLogin;
    // },
    // getUserToken: (state) => state.userToken,
  },

  actions: {
    async getDashBoard() {
      const response = await getDashBoard();
      this.deviceList = response.devicelist;
    },

    getDesktopLayout(): Array<gridItemType> {
      const layout = LocalStorage.getItem(desktopLayoutKey);
      if (layout != null) return layout as Array<gridItemType>;
      else return [];
    },

    getMobileLayout(): Array<gridItemType> {
      const layout = LocalStorage.getItem(mobileLayoutKey);
      if (layout != null) return layout as Array<gridItemType>;
      else return [];
    },

    saveDesktopLayout(layout: Array<gridItemType>) {
      LocalStorage.set(desktopLayoutKey, layout);
    },

    saveMobileLayout(layout: Array<gridItemType>) {
      LocalStorage.set(mobileLayoutKey, layout);
    },

    async setCamToDash(device_id: number, state: boolean) {
      try {
        await setDeviceToDashBoard(device_id, state);
        if (!state) {
          const idx = this.deviceList.findIndex(
            (el) => el.device_id == device_id,
          );
          if (idx >= 0) this.deviceList.splice(idx, 1);
        }
        return true;
      } catch (e) {
        throw e;
      }
    },

    setMobileFullScreen(device_uid: string) {
      const idx = this.deviceList.findIndex(
        (el) => el.device_uid == device_uid,
      );
      if (idx != undefined && idx >= 0) {
        if (this.mobileFullScreen.device_uid == device_uid)
          this.mobileFullScreen.state = !this.mobileFullScreen.state;
        else this.mobileFullScreen = { device_uid, state: true };
      }
    },
  },
});

export type { deviceParams, gridItemType };
