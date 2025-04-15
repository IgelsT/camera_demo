<?php

declare(strict_types=1);

namespace DTO\DataBase;

use DTO\BaseDTO;

class UsersDTO extends BaseDTO
{
    var int $user_id;
    var ?string $user_name;
    var ?string $user_description;
    var string $user_password;
    var string $user_email;
    var ?string $user_hash;
    var ?int $user_confirm;
    var ?string $user_date;
    var ?string $user_lastactivity;
}
