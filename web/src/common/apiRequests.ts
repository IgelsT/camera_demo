// import axios, { AxiosResponse } from 'axios';
import { DeviceToApi } from 'src/stores/DeviceStore';
import { conf } from '../boot/config';
import { localUserStore } from './../stores/LocalUserStore';
import { showSnack } from './popUpMessages';

const Urls = {
  Auth: 'auth',
  Devices: 'device_front',
  DashBoard: 'dashboard',
  // Register: 'register',
  Profile: 'profile',
  // Testing: 'testing',
};

type apiError = {
  code: string;
  message: string;
  reason: string;
};

async function RequestAPI(url: string, data: object) {
  const userStore = localUserStore();
  // console.log(userStore.$state.userToken);
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json;charset=UTF-8',
    Authorization: userStore.$state.userToken,
  };

  let error: apiError | undefined = undefined;
  let response: Response | undefined = undefined;

  try {
    response = await fetch(conf.api_url + url, {
      // mode: 'no-cors',
      method: 'POST',
      headers: headers,
      body: JSON.stringify(data),
      // responseType: isFile ? 'arraybuffer' : 'json',
    });
  } catch (err) {
    error = {
      code: 'NETWORK_ERROR',
      message: (err as Error).message,
      reason: ((err as Error).cause as string) ?? '',
    };

    showSnack(false, error.message);
    // console.log(error);
    throw error;
  }

  if (response) {
    // console.log(response);
    const { data, error } = await analizeResponse(response);
    if (error?.code === 'BAD_TOKEN') userStore.logout('bad token');
    if (error) {
      showSnack(false, error.message);
      throw error;
    }
    return data.data;
  }
}

async function analizeResponse(resp: Response) {
  const contentType = resp.headers.get('Content-Type');
  let data = undefined;
  let error: apiError | undefined = undefined;

  if (!resp.ok) {
    error = {
      code: resp.status.toString(),
      message: resp.statusText,
      reason: '',
    };
    if (contentType == 'application/json') {
      const { body, error_json } = await parseJson(resp);
      if (body) {
        if (body.error) {
          error = {
            code: body.error.code,
            message: body.error.message,
            reason: body.error.reason,
          };
        }
      } else {
        error = error_json;
      }
    }
  }

  if (resp.ok) {
    if (contentType == 'application/json') {
      const { body, error_json } = await parseJson(resp);
      if (body) {
        data = body;
      } else {
        error = error_json;
      }
    } else {
      data = resp;
    }
  }

  return { data, error };
}

async function parseJson(resp: Response) {
  let body = undefined;
  let error_json: apiError | undefined = undefined;
  try {
    body = await resp.json();
  } catch {
    error_json = {
      code: 'ERROR_PARSE_JSON',
      message: 'Error parse json',
      reason: '',
    };
  }

  return { body, error_json };
}

// async function RequestAPIOld(url: string, data: object, isFile = false) {
//   const userStore = localUserStore();
//   // console.log(config)
//   const headers = { Authorization: userStore.$state.userToken };
//   // console.log(headers);
//   const error: apiError = {
//     message: '',
//     code: 0,
//     reason: '',
//   };

//   let response: AxiosResponse;

//   try {
//     response = await axios({
//       url: conf.api_url + url,
//       data: data,
//       headers: headers,
//       method: 'POST',
//       responseType: isFile ? 'arraybuffer' : 'json',
//     });

//     if (response.headers['content-type'] != 'application/json') {
//       return response;
//     } else if (response.data && response.data.result == 'ok') {
//       return response.data.data;
//     } else if (response.data && response.data.result == 'error') {
//       if (response.data.data.error.code === 'BAD_TOKEN')
//         userStore.logout('bad token');
//       error.message = response.data.data.error.message;
//       error.code = response.data.data.error.code;
//       error.reason = response.data.data.error.reason;
//       // showSnack(false, response.data.data);
//       // throw response.data;
//     } else {
//       error.message = 'Empty response';
//       error.code = 2;
//     }
//   } catch (er) {
//     console.log(er);
//     if (er instanceof Error) {
//       error.message = er.message;
//       error.code = 1;
//     }
//     // showSnack(false, error.message);
//     // throw error;
//   }

//   console.log(response);
// }

async function register(email: string, password: string) {
  return RequestAPI(Urls.Auth, {
    action: 'register',
    data: { user_email: email, user_password: password },
  });
}

async function checkHash(hash: string) {
  return RequestAPI(Urls.Auth, {
    action: 'confirmEmail',
    data: { hash },
  });
}

async function recovery(email: string) {
  return RequestAPI(Urls.Auth, {
    action: 'recovery',
    data: { user_email: email },
  });
}

async function apiLogin(username: string, pass: string) {
  return RequestAPI(Urls.Auth, {
    action: 'login',
    data: { user_email: username, user_password: pass },
  });
}

async function apiSaveProfile(data: object) {
  return RequestAPI(Urls.Profile, { action: 'saveProfile', data: data });
}

async function getDeviceList() {
  return RequestAPI(Urls.Devices, {
    action: 'list',
  });
}

async function getDeviceInfo(device_id: number) {
  return RequestAPI(Urls.Devices, {
    action: 'info',
    data: { device_id },
  });
}

async function getDeviceLogs(device_id: number) {
  return RequestAPI(Urls.Devices, {
    action: 'logsList',
    data: { device_id },
  });
}

async function getDeviceLogFile(device_id: number, filename: string) {
  return RequestAPI(Urls.Devices, {
    action: 'logFile',
    data: { device_id, filename },
  });
}

async function apiDeviceDeleteMessages(device_id: number) {
  return RequestAPI(Urls.Devices, {
    action: 'delMsg',
    data: { device_id },
  });
}

async function requestDeviceLogFile(device_id: number, file_name: string) {
  return RequestAPI(Urls.Devices, {
    action: 'requestLogFile',
    data: { device_id, file_name },
  });
}

async function updateDeviceLog(device_id: number) {
  return RequestAPI(Urls.Devices, {
    action: 'requestLogs',
    data: { device_id },
  });
}

async function saveDeviceInfo(params: DeviceToApi) {
  return RequestAPI(Urls.Devices, {
    action: 'saveParams',
    data: { ...params },
  });
}

async function APIdeleteDevice(device_id: number) {
  return RequestAPI(Urls.Devices, {
    action: 'delete',
    data: { device_id },
  });
}

async function getDashBoard() {
  return RequestAPI(Urls.DashBoard, {
    action: 'list',
  });
}

async function setDeviceToDashBoard(device_id: number, state: boolean) {
  return RequestAPI(Urls.DashBoard, {
    action: 'setToDash',
    data: { device_id, state },
  });
}

export {
  apiLogin,
  apiSaveProfile,
  getDeviceList,
  getDeviceInfo,
  saveDeviceInfo,
  apiDeviceDeleteMessages,
  APIdeleteDevice,
  getDashBoard,
  setDeviceToDashBoard,
  register,
  checkHash,
  recovery,
  getDeviceLogs,
  getDeviceLogFile,
  requestDeviceLogFile,
  updateDeviceLog,
};
