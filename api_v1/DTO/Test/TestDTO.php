<?php

namespace DTO\Test;

use DTO\BaseDTO;
use DTO\Utils\ModelAttribute;

class TestDTO extends BaseDTO
{
    var string $action;

    #[ModelAttribute(field: 'data')]
    var TestDTOData $data;

    #[ModelAttribute(field: 'rows', type: TestDTORows::class)]
    var array $rows;

    #[ModelAttribute(field: 'rowsIndexes')]
    var array $rowsIdx;

    var bool $isActive;

    var int $startIdx;

    var float $price;

    var ?int $intTestNull;

    #[ModelAttribute(required: false)]
    var ?int $endIdx = 7;

    #[ModelAttribute(required: false)]
    var int $itemsCount;

    #[ModelAttribute(required: false)]
    var int $safeInt;

    #[ModelAttribute(required: false, defaultValue: -1)]
    var int $safeDefaultInt = 20;
}
