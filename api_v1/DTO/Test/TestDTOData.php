<?php

namespace DTO\Test;

use DTO\BaseDTO;
use DTO\Utils\ModelAttribute;

class TestDTOData extends BaseDTO
{
    var string $device_uid;
    var string $username;
    var string $pass;
    #[ModelAttribute(field: 'rowsInt', type: TestDTORows::class)]
    var array $rows;
}

class TestDTORows
{
    var int $id;
    var string $name;
}
