<?php

namespace App\Http\Controllers;

use App\Exceptions\{ApiException, ERROR_CODES};
use App\Http\Requests\DeviceFront\{
    DeviceID,
    RequestLogFile,
    DownloadLogFile,
    SaveDevice
};
use App\Services\{AuthService, DashboardService, DeviceLogService, DeviceService, MessagesService};

class DeviceFrontController extends Controller
{

    private int $user_id;

    public function __construct(
        private AuthService $authService,
        private DeviceService $deviceService,
        private MessagesService $messagesService
    ) {
        $this->user_id = $authService->getUserId();
    }

    public function list()
    {
        return ['devicelist' => $this->deviceService->getDeviceList($this->user_id)];
    }

    public function info(DeviceID $request)
    {
        $info = $this->deviceService->getDeviceInfo($this->user_id, $request->device_id);
        if ($info === null) throw new ApiException(ERROR_CODES::$DEVICE_NOT_FOUND);

        $camlist = $this->deviceService->getDeviceCams($request->device_id);

        $msglist = $this->messagesService->getMessagesToSend($this->user_id, $info->device_uid);

        return ['deviceinfo' => $info, 'devicecams' => $camlist, 'devicemsg' => $msglist];
    }

    public function saveParams(SaveDevice $request, DashboardService $dashboardService)
    {
        $device = $this->deviceService->checkDeviceByID($request->device_id);

        $this->deviceService->setAccess($request->device_access, $device->device_id);

        $dashboardService->setToDash($this->user_id, $request->device_id, ($request->on_dash) ? 1 : 0);

        $deviceState = $request->toArray()['data'];
        unset($deviceState['device_id']);
        unset($deviceState['device_access']);
        unset($deviceState['on_dash']);
        $deviceState['rtmp_address'] = config('app.app_settings.rtmp_address');

        $this->deviceService->sendSettiongsToDevice($this->user_id, $request->device_id, $device->device_uid, $deviceState);
        $msglist = $this->messagesService->getMessagesToSend($this->user_id, $device->device_uid);

        return ['devicemsg' => $msglist];
    }

    public function delMsg(DeviceID $request)
    {
        $device = $this->deviceService->checkDeviceByID($request->device_id);
        $this->messagesService->deleteMessages($this->user_id, $device->device_uid);
        return ['OK'];
    }

    public function delete(DeviceID $request)
    {
        $device = $this->deviceService->checkDeviceByID($request->device_id);
        $this->deviceService->deleteDevice($device->device_id);
        return ['deleted'];
    }

    public function logsList(DeviceID $request, DeviceLogService $deviceLogService)
    {
        $device = $this->deviceService->checkDeviceByID($request->device_id);
        $logList = $deviceLogService->getLogList($device->device_id, $device->device_uid);

        $msglist = $this->messagesService->getMessagesToSend($this->user_id, $device->device_uid);
        return ['loglist' => $logList, 'devicemsg' => $msglist];
    }

    public function requestLogs(DeviceID $request)
    {
        $device = $this->deviceService->checkDeviceByID($request->device_id);
        $this->deviceService->sendRequestLogsToDevice($this->user_id, $device->device_id, $device->device_uid);

        $msglist = $this->messagesService->getMessagesToSend($this->user_id, $device->device_uid);
        return ['devicemsg' => $msglist];
    }

    public function requestLogFile(RequestLogFile $request)
    {
        $device = $this->deviceService->checkDeviceByID($request->device_id);

        $this->deviceService->sendRequestLogFileToDevice($this->user_id, $device->device_id, $device->device_uid, $request->file_name);

        $msglist = $this->messagesService->getMessagesToSend($this->user_id, $device->device_uid);
        return ['devicemsg' => $msglist];
    }

    public function LogFile(DownloadLogFile $request)
    {
        $device = $this->deviceService->checkDeviceByID($request->device_id);

        $fullFilename = config('app.app_settings.device_logpath') . $device->device_uid . "/" . $request->filename;
        if (!file_exists($fullFilename)) throw new ApiException(ERROR_CODES::$FILE_NOT_FOUND);

        $fileStr = file_get_contents($fullFilename);

        return [
            'file_name' => $request->filename,
            'file64' => base64_encode($fileStr)
        ];
    }
}
