<?php

declare(strict_types=1);

namespace DTO;

use App\DTO\DTOFactory;

class BaseDTO
{
    public function __construct(array | object | null $data = null, bool $safe = false)
    {
        DTOFactory::initFields($this, $data, $safe);
    }

    // public function __construct(array $params = [])
    // {
    //     // $seflFields = get_object_vars($this);
    //     $seflFields = get_class_vars($this::class);
    //     foreach ($seflFields as $key => $value) {
    //         if (array_key_exists($key, $params))
    //             $this->{$key} = $params[$key];
    //     }
    // }
}
