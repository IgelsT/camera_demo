<?php

namespace DTO\Test;

use DTO\BaseDTO;
use DTO\Utils\ModelAttribute;

class TestTableRowsINDTO extends BaseDTO {
    #[ModelAttribute(required: true)]
    var string $row_name;
    #[ModelAttribute(required: true)]
    var string $row_descr;   
    var $idx;
}

class TestTableRowsDTO extends BaseDTO
{
    var int $row_id;
    var string $row_name;
    var ?string $row_descr;
    var ?int $row_status;
    #[ModelAttribute(required: false)]
    var int $rowIdx;
}
