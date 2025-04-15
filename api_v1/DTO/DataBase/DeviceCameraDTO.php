<?php

declare(strict_types=1);

namespace DTO\DataBase;

use DTO\BaseDTO;

class DeviceCameraDTO extends BaseDTO
{
    var int $camera_id;
    var ?int $camera_num;
    var ?string $camera_type;
    var ?string $camera_resolutions;
    var ?string $camera_focuses;
    var int $device_id;
}
