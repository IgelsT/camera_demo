<?php

declare(strict_types=1);

namespace DTO\DataBase;

use DTO\BaseDTO;

class DashboardDTO extends BaseDTO
{
    var int $dash_id;
    var int $device_id;
    var int $user_id;
}
