<?php

declare(strict_types=1);

namespace DTO\DataBase;

use DTO\BaseDTO;

class DeviceStateDTO extends BaseDTO
{
    var int $device_id;
    var ?int $device_camera_id;
    var ?string $device_focus;
    var ?string $device_resolution;
    var ?string $device_orientation;
    var ?int $device_fps;
    var ?int $device_quality;
    var ?int $device_power;
    var ?int $device_status;
    var ?string $device_lastactivity;
    var ?int $device_online;
    var ?string $device_location;
}
