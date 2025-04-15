<?php

declare(strict_types=1);

namespace DTO\DataBase;

use DTO\BaseDTO;

class DeviceLogsDTO extends BaseDTO
{
    var int $log_id;
    var ?string $log_name;
    var ?int $device_id;
}
