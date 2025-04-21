<?php

namespace App\Http\Controllers;

use App\Http\Requests\Device\{
    CameraList,
    DeviceInfo,
    DeviceState,
    AppliedMessages,
    ExecutedMessage,
    DeviceLogs
};
use App\Services\AuthService;
use App\Services\DeviceLogService;
use App\Services\DeviceService;
use App\Services\MessagesService;

class DeviceController extends Controller
{

    private string $device_uid;
    private int $user_id;
    private int $device_id;

    public function __construct(
        private AuthService $authService,
        private DeviceService $deviceService,
        private MessagesService $messagesService
    ) {
        $this->device_uid = $authService->getDeviceUid();
        $this->device_id = $authService->getDeviceId();
        $this->user_id = $authService->getUserId();
    }

    public function setCameraList(CameraList $request)
    {
        foreach ($request->cameralist as $key => $cam) {
            $this->deviceService->setDeviceCamList(
                $this->user_id,
                $this->device_uid,
                $cam['cameraID'],
                $cam['facing'],
                $cam['res'],
                $cam['focuses']
            );
        }
        return ['OK'];
    }

    public function setDeviceState(DeviceState $request)
    {
        $this->deviceService->setDeviceState($this->user_id, $request->state);
        return ['setDeviceStateResponse' => 'OK'];
    }

    public function setDeviceInfo(DeviceInfo $request): array
    {
        $this->deviceService->updateInfo($this->device_id, $request->info);
        return ["OK"];
    }

    public function ping(): array
    {
        $this->deviceService->updateDeviceTime($this->user_id, $this->device_id);

        $messages = $this->messagesService->getMessagesToSend($this->user_id, $this->device_uid);
        if (count($messages) != 0) return ['OK' => 'OK', 'messages' => $messages];
        return ['OK'];
    }

    function appliedMessages(AppliedMessages $request)
    {
        $this->messagesService->applyMessages($this->user_id, $this->device_uid, $request->messages);
        return ['OK'];
    }

    function executedMessages(ExecutedMessage $request)
    {
        $this->messagesService->executedMessage($this->user_id, $this->device_uid, $request->message_id);
        return ['OK'];
    }

    public function setLogList(DeviceLogs $request, DeviceLogService $deviceLogService)
    {
        $deviceLogService->updateLogList($request->logs, $this->device_id);
        return ['OK'];
    }

    public function sendLog()
    {
        $result = false;
        // LogClass::LogV($_FILES);
        foreach ($_FILES as $file) {
            $path = config('app.app_settings.device_logpath') . $this->device_uid;
            if (!is_dir($path)) {
                mkdir($path);
            }
            $target = $path . "/" . $file['name'];
            $result = move_uploaded_file($_FILES['file']['tmp_name'], $target);
        }
        return ['result' => $result];
    }
}
