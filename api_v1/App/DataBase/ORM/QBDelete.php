<?php

declare(strict_types=1);

namespace App\DataBase\ORM;

use App\ApiError;
use App\ERROR_CODES;

interface IQBDeleteWhere extends IQBDeleteWhereStr, IQBDeleteWhereID {}

interface IQBDeleteFromWhere extends IQBDeleteFrom, IQBDeleteWhere {}

interface IQBDeleteFrom
{
    function from(string $table): IQBDeleteWhere;
}

interface IQBDeleteWhereStr
{
    function where(string $whereStr, array $params): IQBDeleteExec;
}

interface IQBDeleteWhereID
{
    function whereId(mixed $id): IQBDeleteExec;
}

interface IQBDeleteExec
{
    function exec(): int;
}

class QBDelete implements IQBDeleteFromWhere, IQBDeleteExec
{
    private string $sqlStr = "";
    private string $whereStr = '';
    private array $params = [];

    public function __construct(private string $table, private string $id, private \Closure $callback) {}

    function from(string $table): IQBDeleteWhere
    {
        $this->table = $table;
        return $this;
    }


    function where(string $whereStr, array $params): IQBDeleteExec
    {
        $this->whereStr = $whereStr;
        $this->params = $params;
        return $this;
    }

    function whereId(mixed $id): IQBDeleteExec
    {
        $this->whereStr = "{$this->id} = :{$this->id}";
        $this->params[":{$this->id}"] = $id;
        return $this;
    }

    function exec(): int
    {
        if ($this->whereStr != '' && count($this->params) > 0) {
            $this->sqlStr = "DELETE FROM {$this->table} WHERE {$this->whereStr}";
            $callback = $this->callback;

            /** @var int */
            return $callback($this->sqlStr, $this->params);
        } else {
            throw new ApiError(ERROR_CODES::$DB_REQUEST_ERROR, "empty where condidion on delete query");
        }
    }
}
