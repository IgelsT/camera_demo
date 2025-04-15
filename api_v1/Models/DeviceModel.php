<?php

declare(strict_types=1);

namespace Models;

use App\DataBase\ORM\BasicModel;
use App\ApiError;
use App\ERROR_CODES;
use DTO\DeviceParams\DeviceDTO;
use DTO\DeviceParams\DPToDeviceDTO;
use DTO\DeviceParams\DPFromDeviceDTO;
use DTO\MessageToDeviceDTO;

interface IDeviceModel
{
    public function getDeviceList(int $user_id);
    public function getDeviceInfo($user_id, $device_id);
    public function getDeviceCams($device_id);
    public function setAccess($acces, $device_id);
    public function setDeviceState(int $user_id, DPFromDeviceDTO $state);
    public function setDeviceCamList(
        int $user_id,
        string $device_uid,
        int $camera_num,
        string $camera_type,
        array $resolutions,
        array $focuses
    );
    public function updateDeviceTime(int $user_id, string $device_uid);
    public function deleteDevice(int $user_id, int $device_id);
    public function sendSettiongsToDevice(int $user_id, int $device_id, string $device_uid, DPToDeviceDTO $device_state);
    public function sendRequestLogsToDevice(int $user_id, int $device_id, string $device_uid);
    public function sendRequestLogFileToDevice(int $user_id, int $device_id, string $device_uid, string $filename);
    public function saveDevice(DeviceDTO $device);
    public function getDeviceByID(int $user_id, int $device_id, bool $all = false): DeviceDTO | bool;
    public function getDeviceByUID(int $user_id, string $device_uid, bool $all = false): DeviceDTO | bool;
    public function getDeviceByToken(string $device_token, bool $all = false): DeviceDTO | bool;
    public function checkDeviceByID(int $user_id, int $device_id): DeviceDTO;
    public function checkDeviceByUID(int $user_id, string $device_uid): DeviceDTO;
    public function checkDeviceLimit(int $user_id, string $device_uid);
}

class DeviceModel extends BasicModel implements IDeviceModel
{
    protected string $_table = 'devices';
    protected string $_id = 'device_id';
    private $deviceLimit = 3;
    private $publicDevices = 1;


    public function getDeviceList(int $user_id)
    {
        $query = 'SELECT devices.device_id, device_name, device_power, device_status, device_online
              FROM devices
              LEFT JOIN device_state ON devices.device_id = device_state.device_id
              WHERE device_deleted = 0 AND user_id = :user_id';
        return $this->query($query, ['user_id' => $user_id])->getRows();
    }

    public function getDeviceInfo($user_id, $device_id)
    {
        $query = "SELECT devices.device_id, device_uid, device_name, device_description, device_info, device_access
            , device_camera_id, device_focus, device_resolution, device_orientation, device_fps, device_quality
            , device_power, device_status, device_lastactivity, device_online
            , if(ifnull(dashboard.dash_id, 0)=0,0,1) AS on_dash
            FROM devices
            LEFT JOIN device_state ON devices.device_id = device_state.device_id
            LEFT JOIN dashboard ON devices.device_id = dashboard.device_id AND dashboard.user_id = devices.user_id 
            WHERE devices.user_id = :user_id AND devices.device_id = :device_id";
        //echo $query; exit;
        return $this->query($query, ['user_id' => $user_id, 'device_id' => $device_id])->getRow();
    }

    public function getDeviceCams($device_id)
    {
        $query = "SELECT * FROM device_camera WHERE device_id = :device_id";
        return $this->query($query, ['device_id' => $device_id])->getRows();
    }

    public function setAccess($acces, $device_id)
    {
        $this->update()->values(['device_access' => $acces])->whereId($device_id)->exec();
    }

    public function setDeviceState(int $user_id, DPFromDeviceDTO $state)
    {
        $this->checkDeviceLimit($user_id, $state->device_uid);
        $device = $this->checkDeviceByUID($user_id, $state->device_uid);

        $params = [
            $user_id,
            $state->device_uid,
            $state->device_name,
            $state->device_description,
            $state->device_camera_id,
            $state->device_focus,
            $state->device_resolution,
            $state->device_orientation,
            $state->device_fps,
            $state->device_quality,
            $state->device_power,
            $state->device_status,
            $state->device_location
        ];
        $this->callPrc('pr_update_device_state', $params);
    }

    public function setDeviceCamList(
        int $user_id,
        string $device_uid,
        int $camera_num,
        string $camera_type,
        array $resolutions,
        array $focuses
    ) {
        $this->checkDeviceLimit($user_id, $device_uid);
        $device = $this->checkDeviceByUID($user_id, $device_uid);
        $res =  implode(',', array_map(function ($var) {
            return implode('x', $var);;
        }, $resolutions));
        $foc = \implode(',', $focuses);
        $params = [$user_id, $device_uid, $camera_num, $camera_type, $res, $foc];
        $this->callPrc('pr_update_camera', $params);
    }

    public function updateDeviceTime(int $user_id, string $device_uid)
    {
        $this->checkDeviceLimit($user_id, $device_uid);
        $device = $this->checkDeviceByUID($user_id, $device_uid);
        $query = "UPDATE device_state ds
              INNER JOIN devices d ON ds.device_id = d.device_id
              SET ds.device_lastactivity = NOW(), ds.device_online = 1
              WHERE device_uid = :device_uid AND user_id = :user_id";
        $this->query($query, ['device_uid' => $device_uid, 'user_id' => $user_id]);
    }

    public function deleteDevice(int $user_id, int $device_id)
    {
        $device = $this->checkDeviceByID($user_id, $device_id);
        $this->update()->values(['device_deleted' => 1])->whereId($device_id)->exec();
    }

    public function sendSettiongsToDevice(int $user_id, int $device_id, string $device_uid, DPToDeviceDTO $device_state)
    {
        $message = json_encode(new MessageToDeviceDTO("settings", $device_state));
        $this->callPrc('pr_message_to_device', [$user_id, $device_id, $device_uid, $message]);
    }

    public function sendRequestLogsToDevice(int $user_id, int $device_id, string $device_uid)
    {
        $message = json_encode(new MessageToDeviceDTO("getLogs", null));
        $this->callPrc('pr_message_to_device', [$user_id, $device_id, $device_uid, $message]);
    }

    public function sendRequestLogFileToDevice(int $user_id, int $device_id, string $device_uid, string $filename)
    {
        $message = json_encode(new MessageToDeviceDTO("getLogFile", ['file_name' => $filename]));
        $this->callPrc('pr_message_to_device', [$user_id, $device_id, $device_uid, $message]);
    }

    // -----------------------
    public function saveDevice(DeviceDTO $device)
    {
        $this->checkDeviceLimit($device->user_id, $device->device_uid);
        $this->upsert($device);
    }

    #region --------------------- Check device ----------------------------
    public function getDeviceByID(int $user_id, int $device_id, bool $all = false): DeviceDTO | bool
    {
        $allStr = ($all) ? "" : " AND device_deleted = 0";
        $device = $this->select()->where(
            'device_id = :device_id AND user_id = :user_id' . $allStr,
            ['device_id' => $device_id, 'user_id' => $user_id]
        )->getOne();
        return ($device == false) ? false : new DeviceDTO($device);
    }

    public function getDeviceByUID(int $user_id, string $device_uid, bool $all = false): DeviceDTO | bool
    {
        $allStr = ($all) ? "" : " AND device_deleted = 0";
        $device = $this->select()->where(
            'device_uid = :device_uid AND user_id = :user_id' . $allStr,
            ['device_uid' => $device_uid, 'user_id' => $user_id]
        )->getOne();
        return ($device == false) ? false : new DeviceDTO($device);
    }

    public function getDeviceByToken(string $device_token, bool $all = false): DeviceDTO | bool
    {
        $allStr = ($all) ? "" : " AND device_deleted = 0";
        $device = $this->select()->where("device_token = :device_token $allStr", ['device_token' => $device_token])->getOne();
        return ($device == false) ? false : new DeviceDTO($device);
    }

    public function checkDeviceByID(int $user_id, int $device_id): DeviceDTO
    {
        $device = $this->getDeviceByID($user_id, $device_id);
        if ($device == false) throw new ApiError(ERROR_CODES::$DEVICE_NOT_FOUND);
        return $device;
    }

    public function checkDeviceByUID(int $user_id, string $device_uid): DeviceDTO
    {
        $device = $this->getDeviceByUID($user_id, $device_uid);
        if ($device == false) throw new ApiError(ERROR_CODES::$DEVICE_NOT_FOUND);
        return $device;
    }

    public function checkDeviceLimit(int $user_id, string $device_uid)
    {
        // echo ("device_uid != '" . $device_uid . "' AND user_id = " . $user_id);
        $devices = $this->select()->where(
            "device_deleted = 0 AND device_uid != :device_uid AND user_id = :user_id",
            ['device_uid' => $device_uid, 'user_id' => $user_id]
        )->getAll();
        if (count($devices) >= $this->deviceLimit) throw new ApiError(ERROR_CODES::$DEVICE_LIMIT);
    }
    #endregion --------------------- Check device ----------------------------
}
