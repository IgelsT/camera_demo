<?php

namespace App\Services;

use App\Models\DashBoard;
use Illuminate\Support\Facades\DB;

class DashboardService
{
    public function getDeviceList($user_id)
    {
        $query = 'SELECT devices.device_id, device_uid, device_name
                , device_resolution, device_orientation, device_fps, device_quality, device_power
                FROM dashboard
                INNER JOIN devices ON devices.device_id = dashboard.device_id
                INNER JOIN device_state ON devices.device_id = device_state.device_id
                WHERE dashboard.user_id = :user_id';
        return DB::select($query, ['user_id' => $user_id]);
    }

    public function setToDash(int $user_id, int $device_id, int $is_dash)
    {
        if ($is_dash == 0) {
            DashBoard::where([
                ['user_id', '=', $user_id],
                ['device_id', '=', $device_id],
            ])->delete();
        } else {
            $result = DashBoard::where([
                ['user_id', '=', $user_id],
                ['device_id', '=', $device_id],
            ])->first();

            if ($result == false) {
                (new DashBoard(['user_id' => $user_id, 'device_id' => $device_id]))->save();
            }
        }
    }
}
