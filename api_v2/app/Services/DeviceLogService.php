<?php

namespace App\Services;

use App\Models\DeviceLog;

class DeviceLogService
{

    public function updateLogList(array $logList, int $device_id)
    {
        DeviceLog::where(
            ['device_id' => $device_id]
        )->delete();

        foreach ($logList as $log) {
            DeviceLog::create(['device_id' => $device_id, 'log_name' => $log]);
        }
    }

    public function getLogList(int $device_id, string $device_uid)
    {
        $logList = DeviceLog::where(
            ['device_id' => $device_id]
        )->get();

        $result = [];
        $logPath = config('app.app_settings.device_logpath');
        foreach ($logList as $log) {
            $log['file'] = file_exists($logPath . $device_uid . '/' . $log['log_name']);
            $result[] = $log;
        }
        return $result;
    }
}
