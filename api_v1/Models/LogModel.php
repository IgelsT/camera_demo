<?php

declare(strict_types=1);

namespace Models;

use App\Vars;
use App\DataBase\ORM\BasicModel;
use DTO\TestDTO;

class LogModel extends BasicModel
{
    protected string $_table = 'device_logs';
    protected string $_id = 'log_id';
    protected $_fields = ['log_id', 'log_name', 'device_id'];

    public function updateLogList(array $logList, int $device_id)
    {
        $this->where("device_id = :device_id", ['device_id' => $device_id])->delete();

        $this->_fields["device_id"] = $device_id;
        foreach ($logList as $log) {
            $this->_fields['log_name'] = $log;
            $this->insert();
        }
    }

    public function getLogList(int $device_id)
    {
        $logList = $this->where("device_id = :device_id", ['device_id' => $device_id])->getAll();
        $result = [];
        $logPath = Vars::s()['deviceLogPath'];
        foreach ($logList as $log) {
            $log['file'] = file_exists($logPath . $log['log_name']);
            $result[] = $log;
        }
        return $result;
    }
}
