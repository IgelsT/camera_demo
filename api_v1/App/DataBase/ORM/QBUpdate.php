<?php

declare(strict_types=1);

namespace App\DataBase\ORM;

use App\ApiError;
use App\ERROR_CODES;

interface IQBUpdateToValues extends IQBUpdateTo, IQBUpdateValues {}

interface IQBUpdateTo
{
    function to(string $table): IQBUpdateValues;
}

interface IQBUpdateValues
{
    function values(object | array $object): IQBUpdateWereExec;
}

interface IQBUpdateWereExec extends IQBUpdateWhere, IQBUpdateWhereID, IQBUpdateExec {}

interface IQBUpdateWhere
{
    function where(string $whereStr, array $params): IQBUpdateExec;
}

interface IQBUpdateWhereID
{
    function whereId(mixed $id): IQBUpdateExec;
}

interface IQBUpdateExec
{
    function exec(): int;
}

class QBUpdate implements IQBUpdateToValues, IQBUpdateWereExec
{
    private string $sqlStr = "";
    private string $whereStr = '';
    private array $params = [];

    public function __construct(private string $table, private string $id, private \Closure $callback) {}

    function to(string $table): IQBUpdateValues
    {
        $this->table = $table;
        return $this;
    }

    function values(object | array $object): IQBUpdateWereExec
    {
        if (gettype($object) == 'object') $object = get_object_vars($object);

        if ($this->id != '' && array_key_exists($this->id, $object)) {
            $this->whereStr = "{$this->id} = :{$this->id}";
            $this->params[":{$this->id}"] = $object[$this->id];
            unset($object[$this->id]);
        }

        $fields = '';
        foreach ($object as $key => $value) {
            $fields .= "$key = :{$key}, ";
            $this->params[":{$key}"] = $value;
        }
        $fields = rtrim($fields, ', ');
        $this->sqlStr = "UPDATE {$this->table} SET $fields";
        return $this;
    }

    function where(string $whereStr, array $params): IQBUpdateExec
    {
        $this->whereStr = $whereStr;
        $this->params = array_merge($this->params, $params);
        return $this;
    }

    function whereId(mixed $id): IQBUpdateExec
    {
        $this->whereStr = "{$this->id} = :{$this->id}";
        $this->params[":{$this->id}"] = $id;
        return $this;
    }

    function exec(): int
    {
        if ($this->whereStr != '' && count($this->params) > 0) {
            $this->sqlStr .= " WHERE {$this->whereStr}";
        } else {
            throw new ApiError(ERROR_CODES::$DB_REQUEST_ERROR, "empty where condidion on update query");
        }
        $callback = $this->callback;
        return $callback($this->sqlStr, $this->params);
    }
}
