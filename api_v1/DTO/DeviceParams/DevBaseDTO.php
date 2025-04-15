<?php

declare(strict_types=1);

namespace DTO\DeviceParams;

use DTO\BaseDTO;

class DevBaseDTO extends BaseDTO
{
    public string $device_name = "";
    public string $device_description = "";
    public int $device_camera_id = 0;
    public $device_focus;
    public $device_resolution;
    public $device_orientation;
    public $device_fps;
    public $device_quality;
    public $device_status;
}
