<?php

declare(strict_types=1);

namespace DTO\Auth;

use App\DTO\DTOAttribute;
use DTO\BaseDTO;

class RegisterRequest extends BaseDTO
{
    #[DTOAttribute(required: true)]
    public $user_email;
    #[DTOAttribute(required: true)]
    public string $user_password;
}
