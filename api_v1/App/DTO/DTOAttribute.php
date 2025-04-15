<?php

namespace App\DTO;

use Attribute;

#[Attribute]
class DTOAttribute
{
    /**
     * @param string|null $field
     * @param string|null $type
     * @param bool|null   $required
     * @param mixed       $defaultValue
     */
    public function __construct(
        public ?string $field = null,
        public ?string $type = null,
        public ?bool $required = null,
        public $defaultValue = null
    ) {}
}
