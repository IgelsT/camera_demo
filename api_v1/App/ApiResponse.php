<?php

namespace App;

class ApiResponse
{
    private string $result = 'error';
    private string $action = '';
    private int $code = 200;
    private ?array $data;
    private ?array $error;

    public function __construct() {}

    public function setOK(array $data): void
    {
        $this->result = 'ok';
        $this->code = 200;
        $this->addDataArray($data);
    }

    public function setError(ApiError $error, array $data): void
    {
        $this->result = 'error';
        $this->code = $error->httpCode;
        $this->error = [
            "code" => $error->apiCode,
            "message" => $error->getMessage(),
            "reason" => $error->reason
        ];
        $this->error['debug'] = $error->__toString();
    }

    function setResult(bool $result): void
    {
        $this->result = ($result) ? 'ok' : 'error';
    }

    function setCode(int $code): void
    {
        $this->code = $code;
    }

    function getCode(): int
    {
        return $this->code;
    }

    function setAction(string $action = ''): void
    {
        $this->action = $action;
    }

    function getAction(): string
    {
        return $this->action;
    }

    function addData(string $key, mixed $value): void
    {
        $this->data[$key] = $value;
    }

    function addDataArray(array $value): void
    {
        if (count($value) > 0) {
            foreach ($value as $k => $v) {
                $this->data[$k] = $v;
            }
        }
    }

    function removeData(string $key): void
    {
        if ($this->data != null)
            unset($this->data[$key]);
    }

    public function toJSON(): string
    {
        return json_encode(get_object_vars($this));
    }
}
