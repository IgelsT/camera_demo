<?php

declare(strict_types=1);

namespace DTO\DataBase;

use DTO\BaseDTO;

class TestTableRowsDTO extends BaseDTO
{
    var int $row_id;
    var ?string $row_name;
    var ?string $row_descr;
    var ?int $row_status;
}
