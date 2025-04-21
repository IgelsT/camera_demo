<?php

declare(strict_types=1);

namespace Models;

use App\Vars;
use App\DataBase\ORM\BasicModel;
use DTO\DataBase\DeviceLogsDTO;

class LogModel extends BasicModel
{
    protected string $_table = 'device_logs';
    protected string $_id = 'log_id';
    protected $_fields = ['log_id', 'log_name', 'device_id'];

    public function updateLogList(array $logList, int $device_id)
    {
        $this->delete()->where("device_id = :device_id", ['device_id' => $device_id])->exec();

        $this->_fields["device_id"] = $device_id;
        foreach ($logList as $log_name) {
            $log = new DeviceLogsDTO();
            $log->device_id = $device_id;
            $log->log_name = $log_name;
            $this->upsert($log);
        }
    }

    public function getLogList(int $device_id, string $device_uid)
    {
        $logList = $this->select()->where("device_id = :device_id", ['device_id' => $device_id])->getAll();
        $result = [];
        $logPath = Vars::s()['deviceLogPath'];
        foreach ($logList as $log) {
            $log['file'] = file_exists($logPath . $device_uid . "/" . $log['log_name']);
            $result[] = $log;
        }
        return $result;
    }
}
