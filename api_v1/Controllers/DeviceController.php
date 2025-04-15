<?php

declare(strict_types=1);

namespace Controllers;

use App\LogClass;
use App\Vars;
use App\ApiError;
use App\ERROR_CODES;
use App\IVars;
use Models\DeviceModel;
use DTO\DeviceParams\DPFromDeviceDTO;
use DTO\DeviceParams\DeviceDTO;
use Models\LogModel;
use Models\MessagesModel;

class DeviceController
{
    /** @var DeviceModel */
    private $deviceModel;

    private IVars $vars;

    private int $user_id;

    private $device_uid;

    /** @var DeviceDTO */
    var DeviceDTO $device;

    function __construct()
    {
        $this->deviceModel = new DeviceModel();
        $this->vars = Vars::getInstance();
        $this->user_id = $this->vars->getUserId();
        $this->device = $this->vars->getDevice();
    }

    private function checkDeviceByUID()
    {
        $this->device = Vars::d();
        if ($this->device == null) throw new ApiError(ERROR_CODES::$DEVICE_NOT_FOUND);
        $this->device_uid = Vars::d()->device_uid;
        // $this->device = $this->deviceModel->checkDeviceByUID($this->user_id, $this->device_uid);
    }

    public function setDeviceInfo()
    {
        $this->checkDeviceByUID();
        $this->device->device_info = json_encode($this->vars->getRequest()->checkParam('info'));
        $this->deviceModel->saveDevice($this->device);
        return ["OK"];
    }

    public function setDeviceState()
    {
        $this->checkDeviceByUID();
        $state = $this->vars->getRequest()->checkParam('state');
        $deviceState = new DPFromDeviceDTO($state);
        $this->deviceModel->setDeviceState($this->user_id, $deviceState);
        return ['setDeviceStateResponse' => 'OK'];
    }

    public function setCameraList()
    {
        $this->checkDeviceByUID();
        $camlist = $this->vars->getRequest()->checkParam('cameralist', "no cameralist array");

        foreach ($camlist as $key => $cam) {
            $this->deviceModel->setDeviceCamList(
                Vars::u()->user_id,
                $this->device_uid,
                $cam['cameraID'],
                $cam['facing'],
                $cam['res'],
                $cam['focuses']
            );
        }
        return ['OK'];
    }

    public function ping()
    {
        $this->checkDeviceByUID();
        $this->deviceModel->updateDeviceTime($this->user_id, $this->device_uid);

        $messagesModel = new MessagesModel();
        $messages = $messagesModel->getMessagesToSend($this->user_id, $this->device_uid);
        if ($messages != false) return ['OK' => 'OK', 'messages' => $messages];
        return ['OK'];
    }

    function appliedMessages()
    {
        $this->checkDeviceByUID();
        $messages = $this->vars->getRequest()->checkParam('messages');

        $messagesModel = new MessagesModel();
        $messagesModel->applyMessages($this->user_id, $this->device_uid, $messages);
        return ['OK'];
    }

    function executedMessages()
    {
        $this->checkDeviceByUID();
        $messageId = $this->vars->getRequest()->checkParam('message_id');

        $messagesModel = new MessagesModel();
        $messagesModel->executedMessage($this->user_id, $this->device_uid, $messageId);
        return ['OK'];
    }

    public function setLogList()
    {
        $this->checkDeviceByUID();
        $logs = $this->vars->getRequest()->checkParam('logs');

        $logModel = new LogModel();
        $logModel->updateLogList($logs, $this->device->device_id);

        return ['OK'];
    }

    public function sendLog()
    {
        $this->checkDeviceByUID();
        $result = false;
        // LogClass::LogV($_FILES);
        foreach ($_FILES as $file) {
            $path = Vars::s()['deviceLogPath'] . $this->device_uid;
            if (!is_dir($path)) {
                mkdir($path);
            }
            $target = $path . "/" . $file['name'];
            $result = move_uploaded_file($_FILES['file']['tmp_name'], $target);
        }
        return ['result' => $result];
    }

    public function requestLogs()
    {
        $device_id = $this->vars->getRequest()->checkParam('device_id');
        $user_id = $this->vars->getUserId();
        $deviceModel = new DeviceModel();
        $device = $deviceModel->checkDeviceByID($user_id, $device_id);
        $deviceModel->sendRequestLogsToDevice($user_id, $device_id, $device->device_uid);
    }
}
