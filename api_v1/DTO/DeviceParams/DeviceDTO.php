<?php

declare(strict_types=1);

namespace DTO\DeviceParams;

use DTO\BaseDTO;

class DeviceDTO extends BaseDTO
{
    public int $device_id = 0;
    public string $device_uid = "";
    public string $device_name = "";
    public string $device_description = "";
    public string $device_info = "";
    public int $device_access = 1;
    public int $user_id = 0;
    public string $device_token = "";
    public int $device_deleted = 0;
}
