<?php

declare(strict_types=1);

namespace App\DataBase\ORM;

use App\ApiError;
use App\ERROR_CODES;
use App\DataBase\DataBase;
use App\DataBase\DBError;
use App\DataBase\DBResult;

class BasicModel
{
    protected string $_table = '';
    protected string $_id = '';

    function __construct()
    {
        if ($this->_table == '' || $this->_id == '')
            throw new ApiError(ERROR_CODES::$CREATE_MODEL_ERRROR, $this::class);
    }

    protected function select(array $fields = ['*']): QBSelect
    {
        return new QBSelect(
            $this->_table,
            $this->_id,
            $fields,
            function (string $sqlStr, array $params, bool $one): array {
                // var_dump($sqlStr, $params);
                try {
                    $result = DataBase::query($sqlStr, $params);
                    if ($one) return $result->getRow();
                    return $result->getRows();
                } catch (DBError $e) {
                    throw new ApiError(ERROR_CODES::$DB_REQUEST_ERROR, $e->getMessage());
                }
            }
        );
    }

    protected function delete(): IQBDeleteFromWhere
    {
        return new QBDelete($this->_table, $this->_id, function (string $sqlStr, array $params): int {
            try {
                $result = DataBase::query($sqlStr, $params);
                return $result->rowsCount;
            } catch (DBError $e) {
                throw new ApiError(ERROR_CODES::$DB_REQUEST_ERROR, $e->getMessage());
            }
        });
    }

    protected function insert(bool $withId = false): IQBInsertToValues
    {
        return new QBInsert($this->_table, $this->_id, function (string $sqlStr, array $params): int {
            try {
                $result = DataBase::query($sqlStr, $params);
                return intval($result->id);
            } catch (DBError $e) {
                throw new ApiError(ERROR_CODES::$DB_REQUEST_ERROR, $e->getMessage());
            }
        }, $withId);
    }

    protected function update(): IQBUpdateToValues
    {
        return new QBUpdate($this->_table, $this->_id, function (string $sqlStr, array $params): int {
            try {
                $result = DataBase::query($sqlStr, $params);
                return $result->rowsCount;
            } catch (DBError $e) {
                throw new ApiError(ERROR_CODES::$DB_REQUEST_ERROR, $e->getMessage());
            }
        });
    }

    protected function upsert(object | array $object): array
    {
        if (gettype($object) == 'object') $object = get_object_vars($object);
        if ($this->_id != '' && array_key_exists($this->_id, $object) && $object[$this->_id] > 0) {
            $result = $this->update()->values($object)->exec();
            return [$object[$this->_id], $result];
        } else {
            $result = $this->insert()->values($object)->exec();
            return [$result, 1];
        }
    }

    protected function callPrc(string $prcName, array $params): int
    {
        $newParams = [];
        foreach ($params as $key => $value) {
            $newParams['p_' . $key] = $value;
        }
        $query = "CALL $prcName(" .  implode(',', array_map(function ($var, $key) {
            return ":$key";
        }, $newParams, array_keys($newParams))) . ")";

        try {
            $result = DataBase::query($query, $newParams);
            return $result->rowsCount;
        } catch (DBError $e) {
            throw new ApiError(ERROR_CODES::$DB_REQUEST_ERROR, $e->getMessage());
        }
    }

    // protected function getLastID(): int
    // {
    //     return DataBase::getLastID();
    // }

    protected function query(string $str, array $params): DBResult
    {
        return DataBase::query($str, $params);
    }

    protected function queryRaw(string $str): DBResult
    {
        return DataBase::query($str);
    }
}
