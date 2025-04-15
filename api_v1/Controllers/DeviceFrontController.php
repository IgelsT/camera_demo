<?php

declare(strict_types=1);

namespace Controllers;


use App\ApiError;
use App\ApiRequest;
use App\ERROR_CODES;
use App\IVars;
use App\Vars;
use Models\DeviceModel;
use Models\DashBoardModel;
use DTO\DeviceParams\DPFromUIDTO;
use DTO\DeviceParams\DPToDeviceDTO;
use Models\LogModel;
use Models\MessagesModel;

class DeviceFrontController
{

    /** @var DeviceModel */
    private $deviceModel;
    private IVars $vars;
    private $user_id;
    private $device_id;
    /** @var DeviceDTO */
    private $device;

    function __construct()
    {
        $this->deviceModel = new DeviceModel();
        $this->vars = Vars::getInstance();
        $this->user_id = $this->vars->getUserId();
        $this->device = $this->vars->getDevice();
    }

    private function checkDeviceByID()
    {
        $this->device_id = intval($this->vars->getRequest()->checkParam('device_id'));
        $this->device = $this->deviceModel->checkDeviceByID($this->user_id, $this->device_id);
    }

    public function list()
    {
        return ['devicelist' => $this->deviceModel->getDeviceList($this->user_id)];
    }

    public function info()
    {
        $this->checkDeviceByID();
        $info = $this->deviceModel->getDeviceInfo($this->user_id, $this->device_id);
        if (!$info) throw new ApiError(ERROR_CODES::$DEVICE_NOT_FOUND);

        $camlist = $this->deviceModel->getDeviceCams($this->device_id);

        $messagesModel = new MessagesModel();
        $msglist = $messagesModel->getMessagesToWork($this->user_id, $this->device->device_uid);

        return ['deviceinfo' => $info, 'devicecams' => $camlist, 'devicemsg' => $msglist];
    }

    public function saveParams()
    {
        $this->checkDeviceByID();
        $deviceData = new DPFromUIDTO($this->vars->getRequestData());

        $this->deviceModel->setAccess($deviceData->device_access, $this->device_id);

        $dashModel = new DashBoardModel();
        $dashModel->setToDash($this->user_id, $this->device_id, ($deviceData->on_dash) ? 1 : 0);

        $deviceState = new DPToDeviceDTO($this->vars->getRequestData());
        $deviceState->rtmp_address = Vars::s()['rtmpAddress'];
        $this->deviceModel->sendSettiongsToDevice($this->user_id, $this->device_id, $this->device->device_uid, $deviceState);

        $messagesModel = new MessagesModel();
        $msglist = $messagesModel->getMessagesToWork(Vars::u()->user_id, $this->device->device_uid);
        return ['devicemsg' => $msglist];
    }

    public function delMsg()
    {
        $this->checkDeviceByID();
        $messagesModel = new MessagesModel();
        $messagesModel->deleteMessages($this->user_id, $this->device_id);
        return ['OK'];
    }

    public function delete()
    {
        $this->checkDeviceByID();
        $this->deviceModel->deleteDevice($this->user_id, $this->device_id);
        return ['deleted'];
    }

    public function logsList()
    {
        $this->checkDeviceByID();
        $logModel = new LogModel();
        $logList = $logModel->getLogList($this->device_id);

        $messagesModel = new MessagesModel();
        $msglist = $messagesModel->getMessagesToWork($this->user_id, $this->device->device_uid);
        return ['loglist' => $logList, 'devicemsg' => $msglist];
    }

    public function requestLogs()
    {
        $this->checkDeviceByID();
        $this->deviceModel->sendRequestLogsToDevice($this->user_id, $this->device_id, $this->device->device_uid);

        $messagesModel = new MessagesModel();
        $msglist = $messagesModel->getMessagesToWork(Vars::u()->user_id, $this->device->device_uid);
        return ['devicemsg' => $msglist];
    }

    public function requestLogFile()
    {
        $this->checkDeviceByID();
        $fileName = $this->vars->getRequest()->checkParam('file_name');
        $this->deviceModel->sendRequestLogFileToDevice($this->user_id, $this->device_id, $this->device->device_uid, $fileName);

        $messagesModel = new MessagesModel();
        $msglist = $messagesModel->getMessagesToWork(Vars::u()->user_id, $this->device->device_uid);
        return ['devicemsg' => $msglist];
    }
}
