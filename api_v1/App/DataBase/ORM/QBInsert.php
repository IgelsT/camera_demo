<?php

declare(strict_types=1);

namespace App\DataBase\ORM;

interface IQBInsertToValues extends IQBInsertTo, IQBInsertValues {}

interface IQBInsertTo
{
    function to(string $table): IQBInsertValues;
}

interface IQBInsertValues
{
    function values(object | array $object): IQBInsertExec;
}

interface IQBInsertExec
{
    function exec(): int;
}

class QBInsert implements IQBInsertToValues, IQBInsertExec
{
    private string $sqlStr = "";
    private ?array $params = null;

    public function __construct(
        private string $table,
        private string $id,
        private mixed $callback,
        private bool $withId = false
    ) {}

    function to(string $table): IQBInsertValues
    {
        $this->table = $table;
        return $this;
    }

    function values(object | array $object): IQBInsertExec
    {
        if (gettype($object) == 'object') $object = get_object_vars($object);
        if (!$this->withId && $this->id != '' && array_key_exists($this->id, $object)) unset($object[$this->id]);
        $fields = $values = '';
        /** @psalm-suppress MixedAssignment */
        foreach ($object as $key => $value) {
            $fields .= "$key, ";
            $values .= ":$key, ";
            $this->params[":{$key}"] = $value;
        }
        $fields = rtrim($fields, ', ');
        $values = rtrim($values, ', ');
        $this->sqlStr = "INSERT INTO {$this->table} ($fields) VALUES($values)";
        return $this;
    }

    /** @psalm-suppress MixedInferredReturnType */
    function exec(): int
    {
        $callback = $this->callback;

        /** @var int */
        return $callback($this->sqlStr, $this->params);
    }
}
