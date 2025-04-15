import { defineStore } from 'pinia';
import {
  APIdeleteDevice,
  getDeviceInfo,
  getDeviceList,
  getDeviceLogFile,
  getDeviceLogs,
  saveDeviceInfo,
  updateDeviceLog,
  apiDeviceDeleteMessages,
  requestDeviceLogFile,
} from 'src/common/apiRequests';
import { assingToExistField } from '../../common/utils';

interface DeviceInfo {
  device_id: number;
  device_name: string;
  device_description: string;
  device_camera_id: number;
  device_resolution: string;
  device_focus: string;
  device_fps: number;
  device_orientation: string;
  device_quality: number;
  device_access: number;
  device_status: number;
  device_lastactivity?: string;
  device_online?: number;
  on_dash: number;
}

interface DeviceFromApi extends DeviceInfo {
  device_info: string;
  device_lastactivity: string;
  device_online: number;
  device_power: number;
  device_uid: string;
  user_id: number;
}

type DeviceToApi = DeviceInfo;

type DeviceCameraFromApi = {
  camera_id: number;
  camera_num: number;
  camera_type: string;
  camera_focuses: string;
  camera_resolutions: string;
  device_id: number;
};

type DeviceMessagesFromApi = {
  message_id: number;
  message_status: number;
  message_create_date: string;
};

const deviceStore = defineStore('deviceStore', {
  state: () => ({
    deviceInfo: {} as DeviceFromApi,
    deviceCams: [] as Array<DeviceCameraFromApi>,
    deviceMessages: [] as Array<DeviceMessagesFromApi>,
    deviceLogList: [] as Array<string>,
  }),

  getters: {
    devCams: (state) => state.deviceCams,
    devInfo: (state) => state.deviceInfo,
  },

  actions: {
    async getDeviceList() {
      const result = await getDeviceList();
      return result.devicelist as Array<DeviceFromApi>;
    },

    async getDeviceInfo(deviceId: number) {
      const result = await getDeviceInfo(deviceId);
      // Object.assign(this.deviceInfo, result.deviceinfo);
      // Object.assign(this.deviceCams, []);
      this.$patch({
        deviceInfo: result.deviceinfo,
        deviceCams: result.devicecams,
        deviceMessages: result.devicemsg,
      });
      // this.deviceInfo = result.deviceinfo;
      // this.deviceCams = result.devicecams;
      return true;
    },

    async delDeviceMessages(deviceId: number) {
      const result = await apiDeviceDeleteMessages(deviceId);
      this.$patch({
        deviceMessages: [],
      });
      return result;
    },

    async getDeviceLogs(deviceId: number) {
      const result = await getDeviceLogs(deviceId);
      this.$patch({
        deviceLogList: result['loglist'],
      });
      return result;
    },

    async getDeviceLogFile(deviceId: number, filename: string) {
      const result = await getDeviceLogFile(deviceId, filename);
      return result;
    },

    async updateDeviceLog(deviceId: number) {
      const result = await updateDeviceLog(deviceId);
      this.$patch({
        deviceMessages: result.devicemsg,
      });
      return true;
    },

    async requestLogFile(deviceId: number, name: string) {
      const result = await requestDeviceLogFile(deviceId, name);
      return result;
    },

    deviceToApi() {
      const obj1: DeviceToApi = {
        device_id: 0,
        device_name: '',
        device_description: '',
        device_camera_id: 0,
        device_resolution: '',
        device_focus: '',
        device_fps: 0,
        device_orientation: 'TOP',
        device_quality: 0,
        device_access: 0,
        device_status: 0,
        on_dash: 0,
      };
      return assingToExistField(obj1, this.deviceInfo);
    },

    async saveDeviceParams(params: DeviceToApi) {
      const result = await saveDeviceInfo(params);
      this.$patch({
        deviceMessages: result.devicemsg,
      });
    },

    async deleteDevice(id: number) {
      await APIdeleteDevice(id);
    },
  },
});

const ddlValues = {
  Resolution: [''],
  Focus: [''],
  Access: [
    { id: 1, value: 'закрыт' },
    { id: 2, value: 'общий' },
  ],
  FPS: [5, 10, 15, 20, 25],
  Quality: [
    { id: 0, value: 'плохое' },
    { id: 1, value: 'низкое' },
    { id: 2, value: 'среднее' },
    { id: 3, value: 'хорошее' },
    { id: 4, value: 'максимальное' },
  ],
  Stream: [
    { id: 0, value: 'нет' },
    { id: 1, value: 'да' },
  ],
  Ondash: [
    { id: 0, value: 'нет' },
    { id: 1, value: 'да' },
  ],
  Orientation: [
    { id: 'TOP', value: 'верх' },
    { id: 'LEFT', value: 'левая' },
    { id: 'RIGHT', value: 'правая' },
    { id: 'BOTTOM', value: 'низ' },
  ],
};

export { deviceStore, ddlValues };

export type { DeviceFromApi, DeviceCameraFromApi, DeviceToApi };
