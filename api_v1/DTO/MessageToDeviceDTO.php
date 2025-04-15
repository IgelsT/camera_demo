<?php

declare(strict_types=1);

namespace DTO;

class MessageToDeviceDTO
{
    public string $action;
    public $data;

    public function __construct(string $action, $data)
    {
        $this->action = $action;
        $this->data = $data;
    }
}
