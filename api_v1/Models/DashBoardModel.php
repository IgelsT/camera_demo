<?php

declare(strict_types=1);

namespace Models;

use App\DataBase\ORM\BasicModel;

class DashBoardModel extends BasicModel
{
  protected string $_table = 'dashboard';
  protected string $_id = 'dash_id';

  public function getDeviceList($user_id)
  {
    $query = 'SELECT devices.device_id, device_uid, device_name
              , device_resolution, device_orientation, device_fps, device_quality, device_power
              FROM dashboard
              INNER JOIN devices ON devices.device_id = dashboard.device_id
              INNER JOIN device_state ON devices.device_id = device_state.device_id
              WHERE dashboard.user_id = :user_id';
    return $this->query($query, ['user_id' => $user_id])->getRows();
  }

  public function setToDash(int $user_id, int $device_id, int $is_dash)
  {
    if ($is_dash == 0) $this->delete()->where(
      'user_id=:user_id AND device_id=:device_id',
      ['user_id' => $user_id, 'device_id' => $device_id]
    )->exec();
    else {
      $result = $this->select()->where(
        'user_id=:user_id AND device_id=:device_id',
        ['user_id' => $user_id, 'device_id' => $device_id]
      )->getOne();
      if ($result == false) {
        $this->insert()->values(['user_id' => $user_id, 'device_id' => $device_id])->exec();
      }
    }
  }
}
