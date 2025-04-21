<?php

namespace App\Services;

use App\Exceptions\ApiException;
use App\Exceptions\ERROR_CODES;
use App\Models\Devices;
use App\Utils\Utils;
use Illuminate\Support\Facades\DB;
use stdClass;

class DeviceService
{
    private $deviceLimit = 3;
    private $publicDevices = 1;

    #region --------------------- mobile app ----------------------------

    public function getDeviceToken(string $device_uid, int $user_id, string $user_email): string
    {
        $device = $this->getDeviceByUID($user_id, $device_uid, true);
        if ($device === null) $device = new Devices();

        $first_path = base64_encode(json_encode(['device' => $device_uid, 'user' => $user_email]));
        $second_path = bin2hex(random_bytes(15));
        $token = $first_path . '.' . $second_path;

        $device->user_id = $user_id;
        $device->device_uid = $device_uid;
        $device->device_token = $token;
        $device->device_deleted = 0;
        $this->saveDevice($device);
        return $token;
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

        $res =  implode(',', array_map(function ($var) {
            return implode('x', $var);;
        }, $resolutions));

        $foc = \implode(',', $focuses);
        $params = [$user_id, $device_uid, $camera_num, $camera_type, $res, $foc];
        DB::select(Utils::preparePRC('pr_update_camera', $params));
    }

    public function setDeviceState(int $user_id, $state)
    {
        $this->checkDeviceLimit($user_id, $state['device_uid']);
        $params = [
            $user_id,
            $state['device_uid'],
            $state['device_name'],
            $state['device_description'],
            $state['device_camera_id'],
            $state['device_focus'],
            $state['device_resolution'],
            $state['device_orientation'],
            $state['device_fps'],
            $state['device_quality'],
            $state['device_power'],
            $state['device_status'],
            $state['device_location'],
        ];
        DB::select(Utils::preparePRC('pr_update_device_state', $params));
    }

    public function updateInfo($device_id, $info)
    {
        $device = $this->getDeviceByID($device_id);
        $device->device_info = json_encode($info);
        $device->save();
    }

    public function updateDeviceTime(int $user_id, string $device_id)
    {
        return DB::table('device_state')->where('device_id', '=', $device_id)
            ->update(['device_lastactivity' => DB::raw('NOW()')]);
    }

    #endregion --------------------- mobile app ----------------------------

    public function getDeviceList(int $user_id)
    {
        $query = 'SELECT devices.device_id, device_name, device_power, device_status, device_online
              FROM devices
              LEFT JOIN device_state ON devices.device_id = device_state.device_id
              WHERE device_deleted = 0 AND user_id = :user_id';
        return DB::select($query, ['user_id' => $user_id]);
    }

    public function getDeviceInfo($user_id, $device_id): ?stdClass
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
        $result = DB::select($query, ['user_id' => $user_id, 'device_id' => $device_id]);
        if (count($result) == 0) return null;
        return $result[0];
    }

    public function getDeviceCams($device_id): array
    {
        $query = "SELECT * FROM device_camera WHERE device_id = :device_id";
        return DB::select($query, ['device_id' => $device_id]);
    }

    public function setAccess($acces, $device_id)
    {
        return Devices::where([
            ['device_id', '=', $device_id],
        ])->update(['device_access' => $acces]);
    }


    public function deleteDevice($device_id)
    {
        return Devices::where([
            ['device_id', '=', $device_id],
        ])->update(['device_deleted' => 1]);
    }

    public function sendSettiongsToDevice(int $user_id, int $device_id, string $device_uid, array $device_state)
    {
        $message = ['action' => 'settings', 'data' => $device_state];
        DB::select(Utils::preparePRC('pr_message_to_device', [$user_id, $device_id, $device_uid, json_encode($message)]));
    }

    public function sendRequestLogsToDevice(int $user_id, int $device_id, string $device_uid)
    {
        $message = ['action' => 'getLogs', 'data' => null];
        DB::select(Utils::preparePRC('pr_message_to_device', [$user_id, $device_id, $device_uid, json_encode($message)]));
    }

    public function sendRequestLogFileToDevice(int $user_id, int $device_id, string $device_uid, string $filename)
    {
        $message = ['action' => 'getLogFile', 'data' => ['file_name' => $filename]];
        DB::select(Utils::preparePRC('pr_message_to_device', [$user_id, $device_id, $device_uid, json_encode($message)]));
    }

    // -----------------------
    public function saveDevice(Devices $device)
    {
        $this->checkDeviceLimit($device->user_id, $device->device_uid);
        $device->save();
    }

    #region --------------------- Check device ----------------------------
    public function getDeviceByID(int $device_id, bool $all = false): ?Devices
    {
        $where = [
            ['device_id', '=', $device_id],
        ];
        if (!$all) {
            $where[] = ['device_deleted', '=', 0];
        }
        return Devices::where($where)->first();
    }

    public function getDeviceByUID(int $user_id, string $device_uid, bool $all = false): ?Devices
    {
        $where = [
            ['device_uid', '=', $device_uid],
            ['user_id', '=', $user_id]
        ];
        if (!$all) {
            $where[] = ['device_deleted', '=', 0];
        }
        return Devices::where($where)->first();
    }

    public function getDeviceByToken(string $device_token, bool $all = false): ?Devices
    {
        $where = [
            ['device_token', '=', $device_token],
        ];
        if (!$all) {
            $where[] = ['device_deleted', '=', 0];
        }
        return Devices::where($where)->first();
    }

    public function checkDeviceByID(int $device_id): ?Devices
    {
        $device = $this->getDeviceByID($device_id);
        if ($device === null) throw new ApiException(ERROR_CODES::$DEVICE_NOT_FOUND);
        return $device;
    }

    public function checkDeviceByUID(int $user_id, string $device_uid): ?Devices
    {
        $device = $this->getDeviceByUID($user_id, $device_uid);
        if ($device == null) throw new ApiException(ERROR_CODES::$DEVICE_NOT_FOUND);
        return $device;
    }

    public function checkDeviceLimit(int $user_id, string $device_uid)
    {
        $devices = Devices::where([
            ['device_uid', '!=', $device_uid],
            ['user_id', '=', $user_id],
            ['device_deleted', '=', 0]
        ])->count();
        if ($devices >= $this->deviceLimit) throw new ApiException(ERROR_CODES::$DEVICE_LIMIT);
    }
    #endregion --------------------- Check device ----------------------------
}
