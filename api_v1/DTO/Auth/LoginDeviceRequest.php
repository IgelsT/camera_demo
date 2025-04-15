<?php

declare(strict_types=1);

namespace DTO\Auth;

use DTO\BaseDTO;
use App\DTO\DTOAttribute;

class LoginDeviceRequest extends BaseDTO
{
    #[DTOAttribute(required: true)]
    public $device_uid;
    #[DTOAttribute(required: true)]
    public $user_email;
    #[DTOAttribute(required: true)]
    public string $user_password;
}
