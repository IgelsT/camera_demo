<?php

declare(strict_types=1);

namespace DTO\DataBase;

use DTO\BaseDTO;

class DevicesDTO extends BaseDTO
{
    var int $device_id;
    var string $device_uid;
    var ?string $device_name;
    var ?string $device_description;
    var ?string $device_info;
    var int $device_access;
    var int $user_id;
    var string $device_token;
    var ?int $device_deleted;
}
