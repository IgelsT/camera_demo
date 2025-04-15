<?php

declare(strict_types=1);

namespace Controllers;

use App\IVars;
use Models\DashBoardModel;
use App\Vars;

class DashBoardController
{
  private IVars $vars;
  private int $user_id;

  public function __construct()
  {
    $this->vars = Vars::getInstance();
    $this->user_id = $this->vars->getUserId();
  }

  public function list()
  {
    $dashModel = new DashBoardModel();
    $list = $dashModel->getDeviceList($this->user_id);
    return ['devicelist' => $list];
  }

  public function setToDash()
  {
    $device_id = $this->vars->getRequest()->checkParam('device_id');
    $state = $this->vars->getRequest()->checkParam('state');
    $dashModel = new DashBoardModel();
    $dashModel->setToDash($this->user_id, $device_id, ($state) ? 1 : 0);
    return ['id' => $device_id];
  }
}
