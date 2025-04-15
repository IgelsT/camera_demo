<?php

declare(strict_types=1);

namespace DTO\DataBase;

use DTO\BaseDTO;

class MessagesDTO extends BaseDTO
{
    var int $message_id;
    var ?string $message;
    var ?string $message_type;
    var ?int $message_status;
    var ?int $user_id;
    var ?string $device_uid;
    var ?int $device_id;
    var ?string $message_create_date;
    var ?string $message_sent_date;
}
