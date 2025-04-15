<?php

declare(strict_types=1);

namespace App\DataBase\ORM;

use ReflectionClass;

interface IQBSelectFrom
{
    function from(string $table): IQBSelectWhereQueryOrderByLimitGet;
}

interface IQBSelectWhereQueryOrderByLimitGet extends IQBSelectWhere, IQBSelectWhereID, IQBSelectQuery, IQBSelectOrderBy, IQBSelectLimit, IQBSelectGet {}

interface IQBSelectQuery
{
    function query(string $str, array $params): IQBSelectGet;
}

interface IQBSelectWhere
{
    function where(string $str, array $params): IQBSelectOrderByLimitGet;
}

interface IQBSelectOrderByLimitGet extends IQBSelectOrderBy, IQBSelectLimit, IQBSelectGet {}

interface IQBSelectWhereID
{
    function whereId(mixed $id): IQBSelectGet;
}

interface IQBSelectOrderBy
{
    function orderBy(string $str): IQBSelectLimitGet;
}

interface IQBSelectLimitGet extends IQBSelectLimit, IQBSelectGet {}

interface IQBSelectLimit
{
    function limit(int $from, int $to): IQBSelectGet;
}

interface IQBSelectGet
{
    function getAll(): array;
    function getOne(): array;

    /**
     * @template T
     * @param class-string<T> $model
     * @return ?T
     */
    function entity(string $model);
}

class QBSelect implements
    IQBSelectFrom,
    IQBSelectOrderByLimitGet,
    IQBSelectWhereQueryOrderByLimitGet,
    IQBSelectLimitGet
{

    private ?string $sqlStr = null;
    private array $params = [];
    private string $whereStr = '';
    private string $orderStr = '';
    private array $limitArr = [];

    function __construct(private string $table, private string $id, private array $fields, private \Closure $callback) {}

    function from(string $table): IQBSelectWhereQueryOrderByLimitGet
    {
        $this->table = $table;
        return $this;
    }

    function query(string $str, array $params): IQBSelectGet
    {
        $this->sqlStr = $str;
        $this->params = $params;
        return $this;
    }

    function where(string $str, array $params): IQBSelectOrderByLimitGet
    {
        $this->whereStr = $str;
        $this->params = array_merge($this->params, $params);
        return $this;
    }

    function whereId(mixed $id): IQBSelectGet
    {
        $this->whereStr = "{$this->id} = :{$this->id}";
        $this->params[":{$this->id}"] = $id;
        return $this;
    }

    function orderBy(string $str): IQBSelectLimitGet
    {
        $this->orderStr = $str;
        return $this;
    }

    function limit(int $from, int $to): IQBSelectGet
    {
        $this->limitArr = ['limit_from' => $from, 'limit_to' => $to];
        return $this;
    }

    function getAll(): array
    {
        if ($this->sqlStr == null) $this->buildSelect();
        $callback = $this->callback;
        return $callback($this->sqlStr, $this->params, false);
    }

    function getOne(): array
    {
        $this->limitArr = ['limit_from' => 0, 'limit_to' => 1];
        if ($this->sqlStr == null) $this->buildSelect();
        $callback = $this->callback;
        return $callback($this->sqlStr, $this->params, true);
    }

    private function buildSelect(): void
    {
        $this->sqlStr = "SELECT " . implode(",", $this->fields) . " FROM " . $this->table;
        if ($this->whereStr != '') $this->sqlStr .= " WHERE " . $this->whereStr;
        if ($this->orderStr != '') $this->sqlStr .= " ORDER BY " . $this->orderStr;
        if (isset($this->limitArr['limit_from']) && isset($this->limitArr['limit_to'])) {
            try {
                $from = intval($this->limitArr['limit_from']);
                $to = intval($this->limitArr['limit_to']);
                $this->sqlStr .= " LIMIT $from,$to";
            } catch (\Exception $e) {
            }
        }
    }

    /**
     * @template T
     * @param class-string<T> $model
     * @return ?T
     */
    function entity(string $model)
    {
        $result = $this->getOne();
        if (count($result) == 0) return null;
        $class = new \ReflectionClass($model);
        $entity = $class->newInstance($result);
        return $entity;
    }
}
